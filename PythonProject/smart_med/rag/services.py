# rag/services.py
import re
from typing import List, Optional

from django.db.models import Q
from pgvector.django import CosineDistance

from .models import Chunk, QAPair
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


# ───────────────────────────────
# 유틸
# ───────────────────────────────
def _normalize(s: str) -> str:
    return s.replace(" ", "").strip()


def _normalize_ko_text(s: str) -> str:
    if not s:
        return ""
    return re.sub(r"\s+", "", s).strip()


def extract_med_names(query: str) -> List[str]:
    toks = re.split(r"[^\w가-힣]+", query)
    meds: List[str] = []
    for t in toks:
        if len(t) < 2:
            continue
        if any(h in t for h in MED_NAME_HINT):
            meds.append(t)
    return list(dict.fromkeys(meds))


def extract_disease_and_topic(question: str) -> tuple[Optional[str], Optional[str]]:
    m = re.search(r"([A-Za-z가-힣0-9\-\(\)]+)\s*(증상|원인|치료|검사|진단|합병증|예방)", question)
    if m:
        disease = m.group(1).strip()
        topic = m.group(2).strip()
        return disease, topic
    return None, None


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

    if "약" in q and any(sym in q for sym in SYMPTOM_CATEGORY_MAP.keys()):
        return INTENT_SYMPTOM

    if any(sym in q for sym in SYMPTOM_CATEGORY_MAP.keys()) and any(
        k in q for k in FOLLOWUP_INCLUDE_KEYS
    ):
        return INTENT_EFFICACY

    return INTENT_GENERAL


# ───────────────────────────────
# QA JSONL 기반 RAG
# ───────────────────────────────
def retrieve_qa_pairs(
    query: str,
    k: int = 5,
    max_distance: float = 0.45,
    disease_kw: Optional[str] = None,
    topic_kw: Optional[str] = None,
):
    q_emb = get_embedding(query)

    qs = (
        QAPair.objects
        .annotate(distance=CosineDistance("embedding", q_emb))
        .order_by("distance")[:k]
    )
    items = list(qs)
    if not items:
        return []

    first_dist = float(items[0].distance) if items[0].distance is not None else None
    print(f"[QA-RAG] top_k={len(items)}, first_dist={first_dist}")

    if first_dist is not None and first_dist > max_distance:
        print(f"[QA-RAG] distance too far ({first_dist} > {max_distance}) → 사용 안 함")
        return []

    if disease_kw:
        norm_kw = _normalize_ko_text(disease_kw)

        def has_kw(txt: Optional[str]) -> bool:
            if not txt:
                return False
            return norm_kw in _normalize_ko_text(txt)

        def is_primary_question(txt: Optional[str]) -> bool:
            if not txt:
                return False
            qn = _normalize_ko_text(txt)
            window = max(10, len(norm_kw) * 2)
            return norm_kw in qn[:window]

        def match_topic(txt: Optional[str]) -> bool:
            if not topic_kw:
                return True
            if not txt:
                return False
            return topic_kw in txt

        def select_from_queryset(qs_base):
            cand = [it for it in qs_base if has_kw(it.question) or has_kw(it.answer)]
            if not cand:
                return []

            cand = [it for it in cand if match_topic(it.question)]
            if not cand:
                return []

            primary = [it for it in cand if is_primary_question(it.question)]
            return primary

        primary_items = select_from_queryset(items)

        if primary_items:
            return primary_items

        kw_qs = QAPair.objects.filter(
            Q(question__icontains=disease_kw) | Q(answer__icontains=disease_kw)
        )
        if topic_kw:
            kw_qs = kw_qs.filter(question__icontains=topic_kw)

        if not kw_qs.exists():
            print(f"[QA-RAG] disease_kw='{disease_kw}', topic='{topic_kw}' QA 없음 → discard")
            return []

        kw_qs = kw_qs.annotate(distance=CosineDistance("embedding", q_emb)).order_by("distance")[:k]
        kw_items = list(kw_qs)
        if not kw_items:
            print(f"[QA-RAG] disease_kw='{disease_kw}' 재검색 결과 없음 → discard")
            return []

        first_dist = float(kw_items[0].distance) if kw_items[0].distance is not None else None
        print(f"[QA-RAG] disease_kw='{disease_kw}' 재검색 top_k={len(kw_items)}, first_dist={first_dist}")

        if first_dist is not None and first_dist > max_distance:
            print(f"[QA-RAG] disease_kw 재검색 distance too far ({first_dist} > {max_distance}) → discard")
            return []

        primary_items = select_from_queryset(kw_items)
        if not primary_items:
            print(
                f"[QA-RAG] disease_kw='{disease_kw}', topic='{topic_kw}' 포함 QA는 있지만 "
                f"메인 질환으로 쓰인 QA가 없음 → discard"
            )
            return []

        return primary_items

    return items


# ───────────────────────────────
# 문장 전처리/필터 유틸
# ───────────────────────────────
def _split_and_merge_sentences(text: str) -> List[str]:
    """문장 분리 + 너무 짧은 문장은 다음 문장과 병합."""
    parts = re.split(r"[\n\.!?]", text)
    parts = [p.strip(" -•\t") for p in parts if p.strip(" -•\t")]
    merged: List[str] = []
    i = 0
    while i < len(parts):
        s = parts[i]
        if len(s) < 10 and i + 1 < len(parts):
            s = s + " " + parts[i + 1]
            i += 2
        else:
            i += 1
        merged.append(s.strip())
    return merged


def is_definition_sentence(s: str) -> bool:
    """질환 정의/설명 문장 자동 판별."""
    text = s.strip()

    # 패턴 1: "<무언가>은/는 ~ 질환/질병/감염/상태 ..."
    #  → '증후군', '증상으로'는 빼서 증상 설명 문장은 걸러지지 않게 함
    if re.match(r".+(은|는)\s+.+(질환|질병|감염|상태)\b", text):
        return True

    # 패턴 2: "~ 질환이다/질병이다/감염이다/증후군이다"
    if re.search(r"(질환|질병|감염|증후군)\s*이다", text):
        return True

    # 패턴 3: "~에 의해 발생/감염/유발 ..."
    if re.search(r"(에 의해|에 의한).+(발생|감염|유발)", text):
        return True

    # 패턴 4: "~로 인해 발생"
    if re.search(r"로 인해 발생", text):
        return True

    return False


# ───────────────────────────────
# 질환 QA: 증상/합병증 전용(LLM 미사용)
# ───────────────────────────────
def build_qa_symptom_answer(question: str, qa_answer: str) -> str:
    """증상 질문은 LLM 안 쓰고, 문장 필터만으로 증상 부분만 추출."""
    text = (qa_answer or "").strip()

    cut_keywords = ["치료", "예방", "검사", "진단", "합병증", "X-ray", "X선", "방사선"]
    cut_pos = [text.find(k) for k in cut_keywords if text.find(k) != -1]
    if cut_pos:
        text = text[:min(cut_pos)]

    sentences = _split_and_merge_sentences(text)
    symptom_hints = ["증상", "고열", "발열", "기침", "가래", "객담", "오한", "호흡곤란", "흉통", "두통", "피로", "근육통"]

    picked: List[str] = []
    for s in sentences:
        if is_definition_sentence(s):
            continue
        if "증상" in s or any(h in s for h in symptom_hints):
            picked.append(s)

    picked = list(dict.fromkeys(picked))[:3]

    if not picked:
        picked = ["관련 증상을 문맥에서 명확히 찾기 어렵습니다."]

    picked.append("정확한 진단과 치료 방법은 반드시 의료진과 상담 후 결정해야 합니다.")
    return "\n".join(f"- {p}" for p in picked)


def build_qa_complication_answer(question: str, qa_answer: str) -> str:
    """질환 QAPair.answer에서 합병증 관련 문장만 추출."""
    text = (qa_answer or "").strip()

    cut_keywords = ["치료", "예방"]
    cut_pos = [text.find(k) for k in cut_keywords if text.find(k) != -1]
    if cut_pos:
        text = text[:min(cut_pos)]

    sentences = _split_and_merge_sentences(text)

    comp_hints = ["합병증", "패혈증", "농흉", "폐농양", "호흡부전", "쇼크"]

    picked: List[str] = [
        s for s in sentences
        if not is_definition_sentence(s)
        and ("합병증" in s or any(h in s for h in comp_hints))
    ]

    if not picked:
        picked = [s for s in sentences if not is_definition_sentence(s)][:3]

    picked = [s for s in picked if len(s) >= 6]
    if not picked:
        picked = [text[:200]]

    lines = [f"- {s}" for s in picked]
    lines.append("합병증의 위험도와 치료는 반드시 의료진과 상의해야 합니다.")
    return "\n".join(lines)


# ───────────────────────────────
# (기타 토픽용) LLM 요약
# ───────────────────────────────
def summarize_qa_answer(question: str, qa_answer: str, topic_kw: Optional[str] = None) -> str:
    """
    증상/합병증 외 토픽은 LLM으로 요약.
    정의 문장, 치료/예방 등은 필터링 최대한 적용.
    """
    raw = qa_answer or ""
    if len(raw) > 2000:
        raw = raw[:2000]

    if topic_kw == "증상":
        cut_keywords = ["치료", "예방", "검사", "진단", "합병증", "X-ray", "X선", "방사선"]
        cut_pos_list = [raw.find(k) for k in cut_keywords if raw.find(k) != -1]
        if cut_pos_list:
            raw = raw[:min(cut_pos_list)]

    sentences = _split_and_merge_sentences(raw)
    sentences = [s for s in sentences if not is_definition_sentence(s)]

    selected: List[str] = []

    topic_filters = {
        "증상": ["증상", "고열", "발열", "기침", "가래", "객담", "오한", "호흡곤란", "흉통", "두통", "피로"],
        "합병증": ["합병증", "패혈증", "농흉", "폐농양", "호흡부전", "쇼크"],
        "원인": ["원인", "감염", "세균", "바이러스", "폐렴구균"],
        "치료": ["치료", "항생제", "항바이러스", "입원", "산소치료", "약물"],
        "예방": ["예방", "백신", "예방접종", "생활습관"],
    }

    if topic_kw in topic_filters:
        hints = topic_filters[topic_kw]
        for s in sentences:
            if any(h in s for h in hints):
                selected.append(s)

        if not selected:
            for s in sentences:
                if topic_kw == "증상":
                    if any(k in s for k in ["치료", "예방", "검사", "진단", "합병증", "X-ray", "X선", "방사선"]):
                        continue
                selected.append(s)
                if len(selected) >= 5:
                    break
    else:
        selected = sentences[:5]

    selected = [s for s in selected if len(s) >= 8]
    if not selected:
        selected = [raw[:300]]

    context = " ".join(selected)
    if len(context) > 600:
        context = context[:600]

    extra_rule = ""
    if topic_kw == "합병증":
        extra_rule = "합병증(어떤 합병증이 생길 수 있는지)만 정리하고 증상·치료·예방 설명은 쓰지 마라."

    prompt = f"""
너는 의학 정보를 간단히 정리하는 한국어 어시스턴트이다.

[사용자 질문]
{question}

[관련 문장들]
{context}

[지시]
1. 위 문장들에서 사용자 질문과 직접 관련된 내용만 뽑아서 정리해라.
2. 최대 3~5개의 bullet 형식으로, 각 bullet은 한 줄(30자 내외)로 작성해라.
3. {extra_rule}
4. 마지막 줄에 '정확한 진단과 치료는 의료진과 상담 후 결정해야 한다'는 취지의 문장을 1줄 넣어라.

[최종 답변]
- 
"""
    raw_answer = generate_answer(prompt).strip()

    if topic_kw == "증상":
        symptom_hints = topic_filters["증상"]
        lines = [ln.strip() for ln in raw_answer.splitlines() if ln.strip()]
        kept: List[str] = []
        disclaimer: Optional[str] = None

        for ln in lines:
            if "정확한 진단" in ln or "의료진과 상담" in ln:
                disclaimer = ln
                continue
            if any(h in ln for h in symptom_hints) or "증상" in ln:
                kept.append(ln)

        if not kept:
            kept = lines[:3]
        if disclaimer:
            kept.append(disclaimer)
        return "\n".join(kept)

    return raw_answer


# ───────────────────────────────
# 약 설명서 기반 RAG
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

    if not chunks:
        print(f"[RAG] no chunks found for query='{query}' (intent={intent}, meds={meds})")
        return []

    first_dist = float(chunks[0].distance) if chunks[0].distance is not None else None
    print(f"[RAG] intent={intent}, meds={meds}, top_k={len(chunks)}, first_dist={first_dist}")

    if not meds and first_dist is not None and first_dist > max_distance:
        print(f"[RAG] distance too far (no meds, {first_dist} > {max_distance}) → 사용 안 함")
        return []

    return chunks


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

    if intent == INTENT_GENERAL:
        disease_kw, topic_kw = extract_disease_and_topic(question)

        if disease_kw:
            qa_items = retrieve_qa_pairs(
                query=question,
                k=3,
                max_distance=0.45,
                disease_kw=disease_kw,
                topic_kw=topic_kw,
            )
            if qa_items:
                top = qa_items[0]
                raw_answer = (top.answer or "").strip()
                print(f"[QA-RAG] hit → disease='{disease_kw}', topic='{topic_kw}'")

                if topic_kw == "증상":
                    return build_qa_symptom_answer(question, raw_answer)
                if topic_kw == "합병증":
                    return build_qa_complication_answer(question, raw_answer)

                return summarize_qa_answer(question, raw_answer, topic_kw)

    if intent == INTENT_COMPARE:
        meds = detect_compare_intent(question) or []
        if len(meds) >= 2:
            return build_compare_answer(question, meds)

    if intent == INTENT_SYMPTOM:
        recs = recommend_by_symptom(question)
        return build_symptom_answer(question, recs)

    if intent == INTENT_SIDE_EFFECT:
        if chunks:
            return build_side_effect_answer(question, chunks)
        return build_general_answer(question, chunks)

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

    return build_general_answer(question, chunks)
