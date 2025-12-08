# rag/services.py
from typing import List
from django.db.models import Q
from pgvector.django import CosineDistance
import torch

from .llm_loader import get_llm_model
from .models import Chunk
from .embeddings import get_embedding
from .llm import generate_answer
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


def serialize_chunks(chunks):
    return [
        {
            "chunk_id": c.chunk_id,
            "item_name": c.item_name,
            "section": c.section,
            "chunk_index": c.chunk_index,
            "text": c.text,
        }
        for c in chunks
    ]

# ---------------------------
# LLM 호출 래퍼
# ---------------------------

def call_llm(prompt: str) -> str:
    tokenizer, model = get_llm_model()

    inputs = tokenizer(
        prompt,
        return_tensors="pt",
        truncation=True,
        max_length=512,
    )
    inputs = {k: v.to(model.device) for k, v in inputs.items()}

    with torch.inference_mode():
        outputs = model.generate(
            **inputs,
            max_new_tokens=256,
            eos_token_id=tokenizer.eos_token_id
        )

    generated_ids = outputs[0][inputs["input_ids"].shape[1]:]
    return tokenizer.decode(generated_ids, skip_special_tokens=True).strip()

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
    exact: List[Chunk] = []
    contains: List[Chunk] = []
    others: List[Chunk] = []

    for c in chunks:
        name = (c.item_name or "").strip()
        if name.startswith(med):
            exact.append(c)
        elif med in name:
            contains.append(c)
        else:
            others.append(c)

    return exact + contains + others


def build_context(chunks: List[Chunk]) -> str:
    blocks: List[str] = []
    for i, c in enumerate(chunks):
        text = (c.text or "")[:MAX_CHUNK_CHARS]
        blocks.append(f"[i={i}] {c.item_name} / {c.section}#{c.chunk_index}")
        blocks.append(text)
        blocks.append("")
    return "\n".join(blocks)


def build_general_answer(question: str, chunks: List[Chunk]) -> str:
    if not chunks:
        prompt = (
            "너는 의약품과 일반 건강 정보를 설명하는 한국어 상담 어시스턴트이다.\n\n"
            "아래 사용자의 질문에 대해, 네가 이미 학습한 의약·건강 지식을 사용해 "
            "안전하고 보수적으로 답변해라. 확실하지 않은 내용이나 진단이 필요한 부분은 "
            "추측하지 말고 반드시 의사 또는 약사 상담을 권고해라.\n"
            "⭐ 답변은 3-5문장 이내로 핵심만 간결하게 작성해라.\n\n"
            f"[질문]\n{question}\n\n[답변]\n"
        )
        return generate_answer(prompt).strip()

    context = build_context(chunks)

    prompt = f"""
너는 의약품과 일반 건강 정보를 설명하는 한국어 상담 어시스턴트이다.

[참고 문서]
{context}

[질문]
{question}

[지시]
1. 위 참고 문서 내용을 우선적으로 활용해 답변해라.
2. 참고 문서에 직접적인 내용이 없더라도,
   네가 이미 학습한 일반 의약 지식을 사용해 최대한 도움이 되게 설명해라.
3. 문서 내용을 그대로 복사하지 말고 핵심만 요약해 한국어로 답변해라.
4. 확실하지 않은 부분은 추측하지 말고 모른다고 말하며,
   필요한 경우 의사 또는 약사 상담을 권고해라.
5. ⭐ 답변은 3-5문장 이내로 핵심만 간결하게 작성해라.

[최종 답변]
"""
    return generate_answer(prompt).strip()


def build_answer(question: str, chunks: List[Chunk]) -> str:
    """
    질문에 대한 최종 답변 생성
    - 의료 질문이 아니면 초기에 차단
    """
    # ========== 의료 질문 필터링 ==========
    if not is_medical_question(question):
        print(f"[FILTER] 비의료 질문 차단: {question[:50]}")
        return get_non_medical_response()
    # =====================================

    intent = detect_intent(question)
    print("[INTENT]", intent, "/ q =", question)

    if intent == INTENT_GENERAL:
        return clean_output(build_general_answer(question, chunks))

    if intent == INTENT_SYMPTOM:
        recs = recommend_by_symptom(question)
        if recs:
            return clean_output(build_symptom_answer(question, recs))

        hf_chunks = search_health_food_chunks(question)
        if hf_chunks:
            return build_health_food_answer(question, hf_chunks)

        return clean_output(build_general_answer(question, chunks))

    if intent == INTENT_SIDE_EFFECT:
        if chunks:
            return clean_output(build_side_effect_answer(question, chunks))
        return clean_output(build_general_answer(question, chunks))

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

    if intent == INTENT_DOSAGE:
        if chunks:
            return clean_output(build_dosage_answer(question, chunks))
        return clean_output(build_general_answer(question, chunks))

    if intent == INTENT_INTERACTION:
        if chunks:
            return clean_output(build_interaction_answer(question, chunks))
        return clean_output(build_general_answer(question, chunks))

    if intent == INTENT_WARNING:
        if chunks:
            return clean_output(build_warning_answer(question, chunks))
        return clean_output(build_general_answer(question, chunks))

    return clean_output(build_general_answer(question, chunks))