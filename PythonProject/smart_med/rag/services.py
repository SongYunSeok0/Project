# rag/services.py
import re
from typing import List, Optional

from django.db.models import Q
from pgvector.django import CosineDistance

from .models import Chunk
from .embeddings import get_embedding
from .llm import generate_answer
from .symptom import SYMPTOM_CATEGORY_MAP, recommend_by_symptom, build_symptom_answer
from .intents import (
    build_side_effect_answer,
    build_efficacy_answer,
    build_dosage_answer,
    build_interaction_answer,
    build_warning_answer,
)


# Intent 정의
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
DOSAGE_KEYS = ["용법", "용량", "복용법", "먹는법", "하루몇번", "하루 몇 번", "몇번", "몇 번", "몇회", "몇 회"]
INTERACTION_KEYS = ["상호작용", "같이먹어도", "같이 먹어도", "병용", "함께복용", "함께 복용"]
WARNING_KEYS = ["주의사항", "주의", "경고", "사용상 주의사항"]

# 약처럼 보이는 토큰 힌트
MED_NAME_HINT = ["정", "캡슐", "액", "시럽", "산", "펜", "콜", "타이레놀", "판콜", "콜드"]


# ───────────────────────────────
# 유틸
# ───────────────────────────────
def _normalize(s: str) -> str:
    return s.replace(" ", "").strip()


def extract_med_names(query: str) -> List[str]:
    """질문에서 약품명 후보만 추출."""
    toks = re.split(r"[^\w가-힣]+", query)
    meds: List[str] = []
    for t in toks:
        if len(t) < 2:
            continue
        if any(h in t for h in MED_NAME_HINT):
            meds.append(t)
    # 중복 제거
    return list(dict.fromkeys(meds))


# ───────────────────────────────
# Intent 감지 + 비교 의도
# ───────────────────────────────
def detect_compare_intent(question: str) -> Optional[List[str]]:
    """타이레놀 vs 판콜에스 같은 비교 질문 여부 + 약 2개 추출."""
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

    # 비교 질문 우선
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

    # 증상 + 약
    if "약" in q and any(sym in q for sym in SYMPTOM_CATEGORY_MAP.keys()):
        return INTENT_SYMPTOM

    return INTENT_GENERAL


# ───────────────────────────────
# 검색 (RAG) - 공통
# ───────────────────────────────
def retrieve_top_chunks(query: str, k: int = 5, max_distance: float = 0.35) -> List[Chunk]:
    intent = detect_intent(query)

    # 일반 질문은 RAG 안 쓰고 LLM만 사용
    if intent == INTENT_GENERAL:
        return []

    q_emb = get_embedding(query)
    meds = extract_med_names(query)

    qs = Chunk.objects.all()

    # intent별 섹션 필터
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

    # 약 이름 필터
    if meds:
        name_q = Q()
        for m in meds:
            name_q |= Q(item_name__icontains=m)
        filtered = qs.filter(name_q)
        if filtered.exists():
            qs = filtered

    qs = qs.annotate(distance=CosineDistance("embedding", q_emb)).order_by("distance")[:k]
    chunks = list(qs)

    if not chunks:
        return []

    # 약 이름이 전혀 없는 경우에만 distance threshold 적용
    if not meds and chunks[0].distance is not None and float(chunks[0].distance) > max_distance:
        return []

    return chunks


# ───────────────────────────────
# 컨텍스트 + 비교 / 일반 LLM
# ───────────────────────────────
MAX_CHUNK_CHARS = 600  # 필요하면 400 정도로 더 줄여도 됨

def build_context(chunks: List[Chunk]) -> str:
    blocks: List[str] = []
    for i, c in enumerate(chunks):
        text = (c.text or "")[:MAX_CHUNK_CHARS]
        blocks.append(f"[{i}] {c.item_name} / {c.section}#{c.chunk_index}")
        blocks.append(text)
        blocks.append("")
    return "\n".join(blocks)

def _extract_after_label(text: str, labels: List[str]) -> str:
    """'효능효과:', '용법용량:' 같은 라벨 이후만 잘라냄."""
    if not text:
        return ""
    for label in labels:
        idx = text.find(label)
        if idx != -1:
            colon_idx = text.find(":", idx)
            if colon_idx != -1:
                return text[colon_idx + 1 :].strip()
            return text[idx + len(label) :].strip()
    return text.strip()


def _select_chunk_for_med(med: str, section_keywords: List[str]) -> Optional[Chunk]:
    """
    특정 약(med)에 대해 원하는 섹션(효능/부작용/용법/주의사항)에 가장 잘 맞는 Chunk 하나 선택
    """
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
    """
    두 약 비교용 답변 생성 (타이레놀 vs 판콜에스 등)
    LLM을 쓰지 않고 문서 내용만 간단 요약해서 직접 비교 문자열 생성
    """
    med_a, med_b = meds[0], meds[1]

    # 약 A
    c_a_eff = _select_chunk_for_med(med_a, ["효능효과", "효능"])
    c_a_se  = _select_chunk_for_med(med_a, ["부작용", "이상반응"])
    c_a_dos = _select_chunk_for_med(med_a, ["용법용량"])
    c_a_war = _select_chunk_for_med(med_a, ["주의", "사용상 주의사항", "경고"])

    eff_a = _extract_after_label(c_a_eff.text, ["효능효과", "효능"]) if c_a_eff else ""
    se_a  = _extract_after_label(c_a_se.text,  ["부작용", "이상반응"]) if c_a_se else ""
    dos_a = _extract_after_label(c_a_dos.text, ["용법용량"]) if c_a_dos else ""
    war_a = _extract_after_label(c_a_war.text, ["주의사항", "주의", "경고", "사용상 주의사항"]) if c_a_war else ""

    # 약 B
    c_b_eff = _select_chunk_for_med(med_b, ["효능효과", "효능"])
    c_b_se  = _select_chunk_for_med(med_b, ["부작용", "이상반응"])
    c_b_dos = _select_chunk_for_med(med_b, ["용법용량"])
    c_b_war = _select_chunk_for_med(med_b, ["주의", "사용상 주의사항", "경고"])

    eff_b = _extract_after_label(c_b_eff.text, ["효능효과", "효능"]) if c_b_eff else ""
    se_b  = _extract_after_label(c_b_se.text,  ["부작용", "이상반응"]) if c_b_se else ""
    dos_b = _extract_after_label(c_b_dos.text, ["용법용량"]) if c_b_dos else ""
    war_b = _extract_after_label(c_b_war.text, ["주의사항", "주의", "경고", "사용상 주의사항"]) if c_b_war else ""

    def _short(s: str, n: int = 80) -> str:
        s = (s or "").replace("\n", " ").strip()
        return s[:n] + ("…" if len(s) > n else "") if s else "문서에 정보 없음"

    lines: List[str] = []

    lines.append("1. 기본 정보")
    lines.append(f"- 약 A({med_a}): { _short(eff_a, 60) }")
    lines.append(f"- 약 B({med_b}): { _short(eff_b, 60) }")
    lines.append("")
    lines.append("2. 효능·효과 비교")
    lines.append(f"- 약 A({med_a}): { _short(eff_a) }")
    lines.append(f"- 약 B({med_b}): { _short(eff_b) }")
    lines.append("")
    lines.append("3. 부작용 비교")
    lines.append(f"- 약 A({med_a}): { _short(se_a) }")
    lines.append(f"- 약 B({med_b}): { _short(se_b) }")
    lines.append("")
    lines.append("4. 용법·용량 비교")
    lines.append(f"- 약 A({med_a}): { _short(dos_a) }")
    lines.append(f"- 약 B({med_b}): { _short(dos_b) }")
    lines.append("")
    lines.append("5. 주의사항 비교")
    lines.append(f"- 약 A({med_a}): { _short(war_a) }")
    lines.append(f"- 약 B({med_b}): { _short(war_b) }")
    lines.append("")
    lines.append("6. 한 줄 요약")
    lines.append("- 두 약 모두 문서에 나온 효능·부작용·주의사항 범위 안에서 선택해야 하며, 실제 복용 전에는 의사 또는 약사와 상의해야 합니다.")

    return "\n".join(lines)



def build_general_answer(question: str, chunks: List[Chunk]) -> str:
    if not chunks:
        q = question.replace(" ", "")

        # 병원/클리닉/의원 + 검색/찾기 관련이면 LLM 안 쓰고 직접 답변
        is_search_hospital = (
            any(k in q for k in ["병원", "클리닉", "의원"])
            and any(k in q for k in ["검색", "찾아", "찾는법", "찾는방법", "어디서", "어디에서"])
        )

        if is_search_hospital:
            return (
                "특정 증상으로 진료받을 병원을 찾을 때는 보통 다음처럼 검색하면 된다.\n\n"
                "1) 검색 키워드 예시\n"
                "- \"증상 이름 + 이비인후과\" (예: 인후두염, 목아픔이면 이비인후과)\n"
                "- \"증상 이름 + 진료 병원\"\n"
                "- \"내 위치 근처 이비인후과\" / \"내 위치 근처 내과\"\n\n"
                "2) 함께 적어주면 좋은 정보\n"
                "- 거주 지역: 예) \"서울 강남구\", \"대전 서구\" 등\n"
                "- 증상: 예) 목 통증, 기침, 속쓰림, 두통, 발열 등\n"
                "- 연령대: 어린이라면 \"소아 이비인후과\", \"소아과\" 같이 적어도 좋음\n\n"
                "3) 어떤 진료과를 찾으면 되는지 (예시)\n"
                "- 목/코/귀/목소리 문제 → 이비인후과\n"
                "- 소화/속쓰림/복통 → 내과\n"
                "- 아이 전체 증상 → 소아청소년과\n\n"
                "4) 바로 응급실을 고려해야 하는 경우\n"
                "- 숨쉬기 힘들 정도의 호흡곤란\n"
                "- 해열제를 써도 39도 이상 고열이 계속되는 경우\n"
                "- 의식이 흐리거나, 심한 흉통/호흡곤란이 동반되는 경우\n"
                "→ 이런 경우에는 119나 응급실을 우선 고려해야 한다."
            )

        # 나머지 일반 자유 질의는 LLM 사용
        prompt = (
            "너는 의약품과 일반 건강 정보를 설명하는 한국어 상담 어시스턴트이다.\n\n"
            "아래 사용자의 질문에 대해, 네가 이미 학습한 의약·건강 지식을 사용해 "
            "안전하고 보수적으로 답변해라. 확실하지 않은 내용이나 진단이 필요한 부분은 "
            "추측하지 말고 반드시 의사 또는 약사 상담을 권고해라.\n\n"
            f"[질문]\n{question}\n\n[답변]\n"
        )
        return generate_answer(prompt).strip()

    # 여기부터는 너가 이미 만든 RAG + LLM 부분 그대로 유지
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

    # 비교 질문 (타이레놀 vs 판콜)
    meds = detect_compare_intent(question)
    if meds:
        return build_compare_answer(question, meds)

    # 증상 기반 추천
    if intent == INTENT_SYMPTOM:
        recs = recommend_by_symptom(question)
        return build_symptom_answer(question, recs)

    # 단일 약 질문들 → 우선 DB 기반 함수 사용
    if intent == INTENT_SIDE_EFFECT:
        if chunks:                    # 문서 있으면 DB 기반 요약
            return build_side_effect_answer(question, chunks)
        return build_general_answer(question, chunks)   # 문서 없으면 LLM

    if intent == INTENT_EFFICACY:
        if chunks:
            return build_efficacy_answer(question, chunks)
        return build_general_answer(question, chunks)

    if intent == INTENT_DOSAGE:
        if chunks:
            return build_dosage_answer(question, chunks)
        return build_general_answer(question, chunks)

    if intent == INTENT_INTERACTION:
        if chunks:
            return build_interaction_answer(question, chunks)
        return build_general_answer(question, chunks)

    if intent == INTENT_WARNING:
        if chunks:
            return build_warning_answer(question, chunks)
        return build_general_answer(question, chunks)

    # 자유 질의 (질환, 일반 건강 질문 등) → LLM
    return build_general_answer(question, chunks)