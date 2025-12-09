# rag/services.py
from typing import List
from django.db.models import Q
from pgvector.django import CosineDistance

from .models import Chunk
from .embeddings import get_embedding
from .symptom import recommend_by_symptom, build_symptom_answer
from .intents import (
    build_side_effect_answer,
    build_efficacy_answer,
    build_dosage_answer,
    build_interaction_answer,
    build_warning_answer,
)
from .constants import (
    INTENT_SIDE_EFFECT,
    INTENT_EFFICACY,
    INTENT_DOSAGE,
    INTENT_INTERACTION,
    INTENT_WARNING,
    INTENT_SYMPTOM,
    INTENT_GENERAL,
    HEALTH_FOOD_KEYS,
    MAX_CHUNK_CHARS,
)
from .utils import (
    normalize,
    clean_output,
    extract_med_names,
    is_medical_question,
    get_non_medical_response,
)
from .intent_detector import detect_intent
from .health_food import search_health_food_chunks, build_health_food_answer


# ================================================================
#  RAG Embedding Search
# ================================================================
def retrieve_top_chunks(query: str, k: int = 3, max_distance: float = 0.5) -> List[Chunk]:
    intent = detect_intent(query)

    if intent == INTENT_GENERAL:
        return []

    q_emb = get_embedding(query)
    meds = extract_med_names(query)

    qs = Chunk.objects.all()

    if intent == INTENT_SIDE_EFFECT:
        qs = qs.filter(Q(section__icontains="부작용") | Q(section__icontains="이상반응"))
    elif intent == INTENT_EFFICACY:
        qs = qs.filter(Q(section__icontains="효능효과") | Q(section__icontains="효능"))
    elif intent == INTENT_DOSAGE:
        qs = qs.filter(Q(section__icontains="용법용량"))
    elif intent == INTENT_INTERACTION:
        qs = qs.filter(Q(section__icontains="상호작용"))
    elif intent == INTENT_WARNING:
        qs = qs.filter(
            Q(section__icontains="사용상 주의사항")
            | Q(section__icontains="주의사항")
            | Q(section__icontains="주의")
            | Q(section__icontains="경고")
        )

    if meds:
        name_q = Q()
        for m in meds:
            name_q |= Q(item_name__icontains=m)
        filtered = qs.filter(name_q)
        if filtered.exists():
            qs = filtered

    qs = qs.annotate(distance=CosineDistance("embedding", q_emb)).order_by("distance")[:k]
    chunks = list(qs)

    if meds:
        chunks = prioritize_base_brand(meds[0], chunks)

    if not chunks:
        print(f"[RAG] no chunks found for query='{query}' (intent={intent}, meds={meds})")
        return []

    first_dist = float(chunks[0].distance) if chunks[0].distance is not None else None
    print(f"[RAG] intent={intent}, meds={meds}, top_k={len(chunks)}, first_dist={first_dist}")

    if not meds and first_dist is not None and first_dist > max_distance:
        print(f"[RAG] distance too far (no meds, {first_dist} > {max_distance}) → 사용 안 함")
        return []

    return chunks


def prioritize_base_brand(med: str, chunks: List[Chunk]) -> List[Chunk]:
    exact, contains, others = [], [], []

    for c in chunks:
        name = (c.item_name or "").strip()
        if name.startswith(med):
            exact.append(c)
        elif med in name:
            contains.append(c)
        else:
            others.append(c)

    return exact + contains + others


# ================================================================
#  Context Builder
# ================================================================
def build_context(chunks: List[Chunk]) -> str:
    blocks: List[str] = []
    for i, c in enumerate(chunks):
        text = (c.text or "")[:MAX_CHUNK_CHARS]
        blocks.append(f"[i={i}] {c.item_name} / {c.section}#{c.chunk_index}")
        blocks.append(text)
        blocks.append("")
    return "\n".join(blocks)


# ================================================================
#  LLM 완전 비활성화 — 여기서 답변을 항상 고정 메시지로 반환
# ================================================================
def build_general_answer(question: str, chunks: List[Chunk]) -> str:
    return "[현재 LLM 기능은 비활성화되어 있어 답변 생성이 중지된 상태입니다.]"


# ================================================================
#  Master Answer Builder (RAG + Intent)
# ================================================================
def build_answer(question: str, chunks: List[Chunk]) -> str:

    # 의료 질문이 아니면 차단
    if not is_medical_question(question):
        print(f"[FILTER] 비의료 질문 차단: {question[:50]}")
        return get_non_medical_response()

    intent = detect_intent(question)
    print("[INTENT]", intent, "/ q =", question)

    # 일반 의약 상담 → LLM 없이 고정 메시지 반환
    if intent == INTENT_GENERAL:
        return clean_output(build_general_answer(question, chunks))

    # 증상 기반 추천
    if intent == INTENT_SYMPTOM:
        recs = recommend_by_symptom(question)
        if recs:
            return clean_output(build_symptom_answer(question, recs))

        hf_chunks = search_health_food_chunks(question)
        if hf_chunks:
            return build_health_food_answer(question, hf_chunks)

        return clean_output(build_general_answer(question, chunks))

    # 부작용
    if intent == INTENT_SIDE_EFFECT:
        if chunks:
            return clean_output(build_side_effect_answer(question, chunks))
        return clean_output(build_general_answer(question, chunks))

    # 효능
    if intent == INTENT_EFFICACY:
        q_norm = normalize(question)
        is_health_food_q = any(h in q_norm for h in HEALTH_FOOD_KEYS)

        if is_health_food_q:
            hf_chunks = search_health_food_chunks(question)
            if hf_chunks:
                return build_health_food_answer(question, hf_chunks)
            return clean_output(build_general_answer(question, []))

        if chunks:
            return clean_output(build_efficacy_answer(question, chunks))

        hf_chunks = search_health_food_chunks(question)
        if hf_chunks:
            return build_health_food_answer(question, hf_chunks)

        return clean_output(build_general_answer(question, []))

    # 용법·용량
    if intent == INTENT_DOSAGE:
        if chunks:
            return clean_output(build_dosage_answer(question, chunks))
        return clean_output(build_general_answer(question, chunks))

    # 상호작용
    if intent == INTENT_INTERACTION:
        if chunks:
            return clean_output(build_interaction_answer(question, chunks))
        return clean_output(build_general_answer(question, chunks))

    # 주의사항·경고
    if intent == INTENT_WARNING:
        if chunks:
            return clean_output(build_warning_answer(question, chunks))
        return clean_output(build_general_answer(question, chunks))

    # fallback
    return clean_output(build_general_answer(question, chunks))
