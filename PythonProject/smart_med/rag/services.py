# rag/services.py
import re
from typing import List, Optional

from django.db.models import Q
from pgvector.django import CosineDistance

from .models import Chunk
from .embeddings import get_embedding
from .llm import generate_answer
from .symptom import (
    SYMPTOM_KEYWORDS,
    recommend_by_symptom,
    build_symptom_answer,
)
from .intents import (
    build_side_effect_answer,
    build_efficacy_answer,
    build_dosage_answer,
    build_interaction_answer,
    build_warning_answer,
)


# ───────────────────────────────
# Intent 정의
# ───────────────────────────────
INTENT_SIDE_EFFECT = "side_effect"
INTENT_EFFICACY = "efficacy"
INTENT_DOSAGE = "dosage"
INTENT_INTERACTION = "interaction"
INTENT_WARNING = "warning"
INTENT_SYMPTOM = "symptom"
INTENT_COMPARE = "compare"
INTENT_GENERAL = "general"

SIDE_EFFECT_KEYS = ["부작용", "이상반응"]
EFFICACY_KEYS = ["효능", "효과", "어디에좋아", "무엇에좋아", "어디에 좋", "무엇에 좋"]

DOSAGE_KEYS = [
    "용법", "용량", "복용법", "먹는법",
    "하루몇번", "하루 몇 번", "몇번", "몇 번", "몇회", "몇 회",
    "언제먹", "언제 먹", "언제 먹어", "언제 복용",
]

INTERACTION_KEYS = ["상호작용", "같이먹어도", "같이 먹어도", "병용", "함께복용", "함께 복용"]
WARNING_KEYS = ["주의사항", "주의", "경고", "사용상 주의사항"]

MED_NAME_HINT = ["정", "캡슐", "액", "시럽", "산", "펜", "콜", "타이레놀", "판콜", "콜드"]

FOLLOWUP_INCLUDE_KEYS = [
    "포함돼", "포함되", "포함안돼", "포함안되",
    "해당돼", "해당되",
    "들어가", "들어있", "들어가있",
]

DISEASE_SUB_SYMPTOM = "symptom"
DISEASE_SUB_CAUSE = "cause"
DISEASE_SUB_COMPLICATION = "complication"
DISEASE_SUB_PREVENTION = "prevention"
DISEASE_SUB_TREATMENT = "treatment"
DISEASE_SUB_GENERAL = "general"


# ───────────────────────────────
# 유틸
# ───────────────────────────────
def _normalize(s: str) -> str:
    return s.replace(" ", "").strip()


def dedupe_sentences(text: str) -> str:
    if not text:
        return ""

    t = text.strip()
    t = re.sub(r"(다|니다|요)\s*\n", r"\1.\n", t)
    parts = re.split(r"(?<=[\.!?])\s+", t)

    out: List[str] = []

    for p in parts:
        s = p.strip()
        if not s:
            continue

        skip = False
        for i, prev in enumerate(out):
            if s == prev:
                skip = True
                break

            if len(s) > len(prev) and (s.startswith(prev) or s.endswith(prev)):
                out[i] = s
                skip = True
                break

            if len(prev) > len(s) and (prev.startswith(s) or prev.endswith(s)):
                skip = True
                break

        if not skip:
            out.append(s)

    return " ".join(out)


def clean_output(text: str) -> str:
    if not text:
        return ""

    t = text.strip()

    n = len(t)
    mid = n // 2
    if n % 2 == 0 and t[:mid] == t[mid:]:
        t = t[:mid].strip()

    paras = []
    seen_paras = set()
    for p in re.split(r"\n{2,}", t):
        p2 = p.strip()
        if not p2:
            continue
        if p2 in seen_paras:
            continue
        seen_paras.add(p2)
        paras.append(p2)
    t = "\n\n".join(paras)

    t = dedupe_sentences(t)
    t = t.replace("\ufffd", "")

    return t.strip()


def extract_med_names(query: str) -> List[str]:
    toks = re.split(r"[^\w가-힣]+", query)
    meds: List[str] = []
    for t in toks:
        if len(t) < 2:
            continue
        if any(h in t for h in MED_NAME_HINT):
            meds.append(t)
    return list(dict.fromkeys(meds))


def extract_disease_keyword(question: str) -> Optional[str]:
    pattern = r"([A-Za-z가-힣0-9\-\(\) ]+?)\s*(?:감염|질환)?\s*(증상|원인|치료|검사|진단|합병증|예방)"
    m = re.search(pattern, question, flags=re.IGNORECASE)
    if not m:
        return None

    disease = m.group(1)
    return disease.strip()


def detect_disease_subintent(question: str) -> str:
    q = question.replace(" ", "")

    if "증상" in q:
        return DISEASE_SUB_SYMPTOM
    if "합병증" in q or "후유증" in q:
        return DISEASE_SUB_COMPLICATION
    if "원인" in q or "왜걸려" in q or "왜생겨" in q:
        return DISEASE_SUB_CAUSE
    if "예방" in q or "예방법" in q or "예방접종" in q or "백신" in q:
        return DISEASE_SUB_PREVENTION
    if "치료" in q or "치료법" in q or "치료방법" in q or "치료는어떻게" in q:
        return DISEASE_SUB_TREATMENT

    return DISEASE_SUB_GENERAL


# ───────────────────────────────
# Intent 감지 + 비교 의도
# ───────────────────────────────
def detect_compare_intent(question: str) -> Optional[List[str]]:
    q = question.replace("VS", "vs").replace("Vs", "vs")
    q = q.replace("비교", "vs")

    toks = re.split(r"[^\w가-힣]+", q)
    meds = [t for t in toks if len(t) >= 2 and any(h in t for h in MED_NAME_HINT)]
    meds = list(dict.fromkeys(meds))

    if len(meds) >= 2:
        return meds[:2]
    return None


def detect_intent(question: str) -> str:
    q = _normalize(question)

    if detect_compare_intent(question):
        return INTENT_COMPARE

    if any(k in q for k in SIDE_EFFECT_KEYS):
        return INTENT_SIDE_EFFECT
    if any(k in q for k in EFFICACY_KEYS):
        return INTENT_EFFICACY
    if any(k in q for k in DOSAGE_KEYS):
        return INTENT_DOSAGE
    if any(k in q for k in INTERACTION_KEYS):
        return INTENT_INTERACTION
    if any(k in q for k in WARNING_KEYS):
        return INTENT_WARNING

    if any(sym in q for sym in SYMPTOM_KEYWORDS) and any(
        k in q for k in FOLLOWUP_INCLUDE_KEYS
    ):
        return INTENT_EFFICACY

    if any(sym in q for sym in SYMPTOM_KEYWORDS):
        return INTENT_SYMPTOM

    return INTENT_GENERAL


# ───────────────────────────────
# 약 설명서 기반 RAG (Chunk만 사용)
# ───────────────────────────────
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
        chunks = _prioritize_base_brand(meds[0], chunks)

    if not chunks:
        print(f"[RAG] no chunks found for query='{query}' (intent={intent}, meds={meds})")
        return []

    first_dist = float(chunks[0].distance) if chunks[0].distance is not None else None
    print(f"[RAG] intent={intent}, meds={meds}, top_k={len(chunks)}, first_dist={first_dist}")

    if not meds and first_dist is not None and first_dist > max_distance:
        print(f"[RAG] distance too far (no meds, {first_dist} > {max_distance}) → 사용 안 함")
        return []

    return chunks


def _prioritize_base_brand(med: str, chunks: List[Chunk]) -> List[Chunk]:
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


# ───────────────────────────────
# 건강기능식품(hf_*) RAG
# ───────────────────────────────
def search_health_food_chunks(query: str, k: int = 5, max_distance: float = 0.6) -> List[Chunk]:
    """
    건강기능식품(hf_*) 전용 Chunk 검색
    """
    q_emb = get_embedding(query)

    qs = (
        Chunk.objects
        .filter(section__startswith="hf_")
        .annotate(distance=CosineDistance("embedding", q_emb))
        .order_by("distance")[:k]
    )

    chunks = list(qs)
    if not chunks:
        return []

    first_dist = float(chunks[0].distance) if chunks[0].distance is not None else None
    print(f"[HF-RAG] top_k={len(chunks)}, first_dist={first_dist}")

    if first_dist is not None and first_dist > max_distance:
        print(f"[HF-RAG] distance too far ({first_dist} > {max_distance}) → 사용 안 함")
        return []

    return chunks


def build_health_food_answer(question: str, chunks: List[Chunk]) -> str:
    """
    건강기능식품 / 영양제 추천·설명용 답변 생성
    (hf_usage / hf_caution / hf_function 섹션 사용)
    """
    if not chunks:
        return build_general_answer(question, [])

    context = build_context(chunks)

    prompt = f"""
너는 '건강기능식품/영양제' 정보를 설명하는 한국어 상담 어시스턴트이다.

[참고 문서]
{context}

[질문]
{question}

[지시]
1. 위 문서에서 사용자의 질문과 가장 관련 있는 건강기능식품 1~3개만 골라라.
2. 각 제품에 대해 다음을 한국어로 정리해라.
   - 제품명
   - 어떤 도움(효과/기능성)을 줄 수 있는지 (기능성)
   - 기본적인 섭취 방법 (있다면)
   - 중요한 주의사항 (있다면)
3. 문서에 없는 내용은 지어내지 말고, '문서에 정보 없음' 수준으로만 언급해라.
4. 질병 진단·치료나 처방은 하지 말고,
   실제 복용 전에는 의사 또는 약사와 상의해야 한다는 점을 함께 언급해라.
5. 목록(bullet)과 짧은 문장 위주로, 이해하기 쉽게 정리해라.

[최종 답변]
"""
    return generate_answer(prompt).strip()


# ───────────────────────────────
# 컨텍스트 + 비교 / 일반 LLM
# ───────────────────────────────
MAX_CHUNK_CHARS = 600


def build_context(chunks: List[Chunk]) -> str:
    blocks: List[str] = []
    for i, c in enumerate(chunks):
        text = (c.text or "")[:MAX_CHUNK_CHARS]
        blocks.append(f"[i={i}] {c.item_name} / {c.section}#{c.chunk_index}")
        blocks.append(text)
        blocks.append("")
    return "\n".join(blocks)


def _extract_after_label(text: str, labels: List[str]) -> str:
    if not text:
        return ""
    for label in labels:
        idx = text.find(label)
        if idx != -1:
            colon_idx = text.find(":", idx)
            if colon_idx != -1:
                return text[colon_idx + 1:].strip()
            return text[idx + len(label):].strip()
    return text.strip()


def _select_chunk_for_med(med: str, section_keywords: List[str]) -> Optional[Chunk]:
    qs = Chunk.objects.filter(item_name__icontains=med)
    if not qs.exists():
        return None

    if section_keywords:
        sec_q = Q()
        for sec in section_keywords:
            sec_q |= Q(section__icontains=sec)
        qs_sec = qs.filter(sec_q)
        if qs_sec.exists():
            qs = qs_sec

    return qs.first()


def build_compare_answer(question: str, meds: List[str]) -> str:
    med_a, med_b = meds[0], meds[1]

    c_a_eff = _select_chunk_for_med(med_a, ["효능효과", "효능"])
    c_a_se = _select_chunk_for_med(med_a, ["부작용", "이상반응"])
    c_a_dos = _select_chunk_for_med(med_a, ["용법용량"])
    c_a_war = _select_chunk_for_med(med_a, ["주의", "사용상 주의사항", "경고"])

    eff_a = _extract_after_label(c_a_eff.text, ["효능효과", "효능"]) if c_a_eff else ""
    se_a = _extract_after_label(c_a_se.text, ["부작용", "이상반응"]) if c_a_se else ""
    dos_a = _extract_after_label(c_a_dos.text, ["용법용량"]) if c_a_dos else ""
    war_a = _extract_after_label(
        c_a_war.text, ["주의사항", "주의", "경고", "사용상 주의사항"]
    ) if c_a_war else ""

    c_b_eff = _select_chunk_for_med(med_b, ["효능효과", "효능"])
    c_b_se = _select_chunk_for_med(med_b, ["부작용", "이상반응"])
    c_b_dos = _select_chunk_for_med(med_b, ["용법용량"])
    c_b_war = _select_chunk_for_med(med_b, ["주의", "사용상 주의사항", "경고"])

    eff_b = _extract_after_label(c_b_eff.text, ["효능효과", "효능"]) if c_b_eff else ""
    se_b = _extract_after_label(c_b_se.text, ["부작용", "이상반응"]) if c_b_se else ""
    dos_b = _extract_after_label(c_b_dos.text, ["용법용량"]) if c_b_dos else ""
    war_b = _extract_after_label(
        c_b_war.text, ["주의사항", "주의", "경고", "사용상 주의사항"]
    ) if c_b_war else ""

    def _short(s: str, n: int = 80) -> str:
        s = (s or "").replace("\n", " ").strip()
        return s[:n] + ("…" if len(s) > n else "") if s else "문서에 정보 없음"

    lines: List[str] = []

    lines.append("1. 기본 정보")
    lines.append(f"- 약 A({med_a}): {_short(eff_a, 60)}")
    lines.append(f"- 약 B({med_b}): {_short(eff_b, 60)}")
    lines.append("")
    lines.append("2. 효능·효과 비교")
    lines.append(f"- 약 A({med_a}): {_short(eff_a)}")
    lines.append(f"- 약 B({med_b}): {_short(eff_b)}")
    lines.append("")
    lines.append("3. 부작용 비교")
    lines.append(f"- 약 A({med_a}): {_short(se_a)}")
    lines.append(f"- 약 B({med_b}): {_short(se_b)}")
    lines.append("")
    lines.append("4. 용법·용량 비교")
    lines.append(f"- 약 A({med_a}): {_short(dos_a)}")
    lines.append(f"- 약 B({med_b}): {_short(dos_b)}")
    lines.append("")
    lines.append("5. 주의사항 비교")
    lines.append(f"- 약 A({med_a}): {_short(war_a)}")
    lines.append(f"- 약 B({med_b}): {_short(war_b)}")
    lines.append("")
    lines.append("6. 한 줄 요약")
    lines.append(
        "- 두 약 모두 문서에 나온 효능·부작용·주의사항 범위 안에서 선택해야 하며, "
        "실제 복용 전에는 의사 또는 약사와 상의해야 합니다."
    )

    return "\n".join(lines)


def build_general_answer(question: str, chunks: List[Chunk]) -> str:
    if not chunks:
        q = question.replace(" ", "")

        is_search_hospital = (
            any(k in q for k in ["병원", "클리닉", "의원"])
            and any(k in q for k in ["검색", "찾아", "찾는법", "찾는방법", "어디서", "어디에서"])
        )

        if is_search_hospital:
            return (
                "특정 증상으로 진료받을 병원을 찾을 때는 보통 다음처럼 검색하면 됩니다.\n\n"
                "1) 검색 키워드 예시\n"
                "- '증상 이름 + 이비인후과' (예: 인후두염, 목아픔이면 이비인후과)\n"
                "- '증상 이름 + 진료 병원'\n"
                "- '내 위치 근처 이비인후과' / '내 위치 근처 내과'\n\n"
                "2) 함께 적어주면 좋은 정보\n"
                "- 거주 지역: 예) '서울 강남구', '대전 서구' 등\n"
                "- 증상: 예) 목 통증, 기침, 속쓰림, 두통, 발열 등\n"
                "- 연령대: 어린이라면 '소아 이비인후과', '소아과' 같이 적어도 좋음\n\n"
                "3) 어떤 진료과를 찾으면 되는지 (예시)\n"
                "- 목/코/귀/목소리 문제 → 이비인후과\n"
                "- 소화/속쓰림/복통 → 내과\n"
                "- 아이 전체 증상 → 소아청소년과\n\n"
                "4) 바로 응급실을 고려해야 하는 경우\n"
                "- 숨쉬기 힘들 정도의 호흡곤란\n"
                "- 해열제를 써도 39도 이상 고열이 계속되는 경우\n"
                "- 의식이 흐리거나, 심한 흉통/호흡곤란이 동반되는 경우\n"
                "→ 이런 경우에는 119나 응급실을 우선 고려해야 합니다."
            )

        prompt = (
            "너는 의약품과 일반 건강 정보를 설명하는 한국어 상담 어시스턴트이다.\n\n"
            "아래 사용자의 질문에 대해, 네가 이미 학습한 의약·건강 지식을 사용해 "
            "안전하고 보수적으로 답변해라. 확실하지 않은 내용이나 진단이 필요한 부분은 "
            "추측하지 말고 반드시 의사 또는 약사 상담을 권고해라.\n\n"
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

[최종 답변]
"""
    return generate_answer(prompt).strip()


# ───────────────────────────────
# Intent 라우팅 (최종)
# ───────────────────────────────
def build_answer(question: str, chunks: List[Chunk]) -> str:
    intent = detect_intent(question)
    print("[INTENT]", intent, "/ q =", question)

    if intent == INTENT_GENERAL:
        disease_kw = extract_disease_keyword(question)
        print("disease_kw =", disease_kw)

        if disease_kw:
            sub = detect_disease_subintent(question)
            print("disease_subintent =", sub)

            if sub == DISEASE_SUB_SYMPTOM:
                instruction = f"""
너는 의료 정보를 설명하는 한국어 상담 어시스턴트이다.

[질환명]
{disease_kw}

[사용자 질문]
{question}

[지시]
- 이 질환에서 일반적으로 알려진 '대표 증상'만 한국어로 정리해라.
- 4~6개 정도의 핵심 증상을 bullet 또는 짧은 문장으로 정리해라.
- 정의, 원인, 감염경로, 진단, 검사, 치료, 예방, 예후, 합병증 등
  증상이 아닌 정보는 쓰지 마라.
- 특정 약 이름, 용량, 처방 지시는 제시하지 마라.
- 확실하지 않은 내용은 적지 말고, 필요한 경우 의사 진료를 권고해라.

[답변]
"""
                result = generate_answer(instruction).strip()
                return clean_output(result)

            if sub == DISEASE_SUB_CAUSE:
                instruction = f"""
너는 의료 정보를 설명하는 한국어 상담 어시스턴트이다.

[질환명]
{disease_kw}

[사용자 질문]
{question}

[지시]
- 이 질환이 발생하는 '주요 원인'과 '위험요인'을 중심으로 설명해라.
- 일반적으로 알려진 내용만 요약해서 설명하고, 개별 환자 진단처럼 말하지 마라.
- 증상, 검사 방법, 치료 방법, 예방 방법, 합병증에 대한 자세한 설명은 하지 마라.
- 특정 약 이름, 검사, 수술 등을 구체적으로 지시하지 말고,
  궁극적인 진단과 치료는 의사가 결정해야 한다고 명시해라.

[답변]
"""
                result = generate_answer(instruction).strip()
                return clean_output(result)

            if sub == DISEASE_SUB_COMPLICATION:
                instruction = f"""
너는 의료 정보를 설명하는 한국어 상담 어시스턴트이다.

[질환명]
{disease_kw}

[사용자 질문]
{question}

[지시]
- 이 질환에서 발생할 수 있는 '대표적인 합병증'을 설명해라.
- 합병증 이름과, 아주 간단한 특징 정도만 설명해라.
- 일반적인 증상, 원인, 예방, 치료 방법에 대한 장황한 설명은 하지 마라.
- 실제로 이런 합병증이 의심되면 반드시 의료진 진료가 필요하다는 점을 강조해라.

[답변]
"""
                result = generate_answer(instruction).strip()
                return clean_output(result)

            if sub == DISEASE_SUB_PREVENTION:
                instruction = f"""
너는 의료 정보를 설명하는 한국어 상담 어시스턴트이다.

[질환명]
{disease_kw}

[사용자 질문]
{question}

[지시]
- 이 질환을 예방하기 위해 일반적으로 권장되는 생활습관, 위생수칙,
  백신/예방접종 여부 등을 정리해라.
- 구체적인 약 이름, 용량, 처방 지시는 하지 말고, '일반적인 건강 수칙' 수준에서 설명해라.
- 이미 증상이 있는 사람은 스스로 판단하지 말고 의사 진료를 받아야 한다는 점을 같이 언급해라.

[답변]
"""
                result = generate_answer(instruction).strip()
                return clean_output(result)

            if sub == DISEASE_SUB_TREATMENT:
                instruction = f"""
너는 의료 정보를 설명하는 한국어 상담 어시스턴트이다.

[질환명]
{disease_kw}

[사용자 질문]
{question}

[지시]
- 이 질환에서 일반적으로 사용되는 '치료/관리 방법'을 큰 틀에서만 설명해라.
- 예: 휴식, 수분 섭취, 해열제·진통제 사용 가능 여부, 항생제/항바이러스제 사용 여부,
  입원이 필요한 상황 등 '원칙' 수준으로만 설명해라.
- 특정 약의 이름, 정확한 용량, 복용 기간 등 구체적 처방은 쓰지 마라.
- 실제 치료 계획은 반드시 의사가 결정해야 하며, 사용자의 상태에 따라 달라질 수 있음을 명시해라.

[답변]
"""
                result = generate_answer(instruction).strip()
                return clean_output(result)

        instruction = f"""
너는 의료 정보를 설명하는 한국어 상담 어시스턴트이다.

[질문]
{question}

[지시]
- 네가 학습한 일반 의학 상식을 사용해, 안전하고 보수적으로 답변해라.
- 확실하지 않은 부분은 추측하지 말고 모른다고 말하며, 의사 또는 약사 상담을 권고해라.
- 진단이나 처방을 내리지 말고, 일반적인 정보 수준에서 설명해라.

[답변]
"""
        result = generate_answer(instruction).strip()
        return clean_output(result)

    if intent == INTENT_COMPARE:
        meds = detect_compare_intent(question) or []
        if len(meds) >= 2:
            return clean_output(build_compare_answer(question, meds))

    if intent == INTENT_SYMPTOM:
        recs = recommend_by_symptom(question)
        return clean_output(build_symptom_answer(question, recs))

    if intent == INTENT_SIDE_EFFECT:
        if chunks:
            return clean_output(build_side_effect_answer(question, chunks))
        return clean_output(build_general_answer(question, chunks))

    if intent == INTENT_EFFICACY:
        if chunks:
            return clean_output(build_efficacy_answer(question, chunks))

        hf_chunks = search_health_food_chunks(question, k=5)
        if hf_chunks:
            return clean_output(build_health_food_answer(question, hf_chunks))

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