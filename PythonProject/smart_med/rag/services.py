# rag/services.py
import re
from typing import List, Optional

from django.db.models import Q
from pgvector.django import CosineDistance

from .models import Chunk, QAPair
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

# "언제 먹어?" 류를 용법/용량으로 인식하도록 키워드
DOSAGE_KEYS = [
    "용법", "용량", "복용법", "먹는법",
    "하루몇번", "하루 몇 번", "몇번", "몇 번", "몇회", "몇 회",
    "언제먹", "언제 먹", "언제 먹어", "언제 복용",
]

INTERACTION_KEYS = ["상호작용", "같이먹어도", "같이 먹어도", "병용", "함께복용", "함께 복용"]
WARNING_KEYS = ["주의사항", "주의", "경고", "사용상 주의사항"]

# 약처럼 보이는 토큰 힌트
MED_NAME_HINT = ["정", "캡슐", "액", "시럽", "산", "펜", "콜", "타이레놀", "판콜", "콜드"]

# 증상 관련 후속질문 힌트 (포함 돼? 안 돼? 등)
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


def normalize_bullets(text: str) -> str:
    """
    '1. 문장 2. 문장 3. 문장'처럼 붙어서 나온 번호 목록을
    줄바꿈된 bullet 형태로 정리한다.
    """
    if not text:
        return text

    pattern = r"(\d+\.\s+)"
    parts = re.split(pattern, text)

    if len(parts) <= 1:
        return text

    out = []
    buffer = ""

    for p in parts:
        if re.fullmatch(pattern, p):
            if buffer.strip():
                out.append(buffer.strip())
            buffer = p
        else:
            buffer += p

    if buffer.strip():
        out.append(buffer.strip())

    return "\n".join(out)


def dedupe_sentences(text: str) -> str:
    """
    QAPair.answer처럼 문장이 그대로 또는 거의 그대로 두 번 반복된 경우
    문장 단위로 중복/부분중복을 제거.
    """
    if not text:
        return ""

    t = text.strip()

    # '다\n', '니다\n', '요\n' → '다.\n' 식으로 문장 경계 보정
    t = re.sub(r"(다|니다|요)\s*\n", r"\1.\n", t)

    # 마침표/물음표/느낌표 뒤 공백 기준 분리 (고정 길이 lookbehind)
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

def dedupe_numbered_bullets(text: str) -> str:
    """
    '1. 문장' 형식의 번호 매긴 bullet 중에서
    내용이 거의 같은 항목을 제거한다.
    - 공백 제거 후 문장 전체를 비교
    - '일반적으로', '또한', '따라서' 같은 앞머리는 무시
    """
    if not text:
        return text

    lines = text.splitlines()
    out: List[str] = []
    seen_norms: List[str] = []

    for ln in lines:
        m = re.match(r"\s*(\d+)\.\s*(.+)", ln)
        if not m:
            out.append(ln)
            continue

        body = m.group(2).strip()
        body_core = re.sub(r"^(일반적으로|또한|따라서)\s*", "", body)
        norm = re.sub(r"\s+", "", body_core)

        skip = False
        for prev in seen_norms:
            # 서로 포함 관계면 의미 중복으로 처리
            if norm == prev or norm in prev or prev in norm:
                skip = True
                break

        if not skip:
            seen_norms.append(norm)
            out.append(ln)

    return "\n".join(out)

def clean_output(text: str) -> str:
    """
    공통 후처리:
    - 전체가 두 번 반복된 형태(AAA...AAA...)면 한 번만 남김
    - 문단/문장 중복 제거
    - 깨진 문자 제거
    - 번호 목록 포맷 보정
    - 번호 bullet 중복 제거
    """
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
    t = normalize_bullets(t)

    # ★ 핵심: bullet 중복 제거 추가
    t = dedupe_numbered_bullets(t)

    return t.strip()


def force_numbered_bullets(text: str, max_n: int = 6) -> str:
    """
    질환 '증상' 답변처럼 꼭 1. 2. 3. 형식으로 정리하고 싶은 경우 사용.
    이미 '1. ' 패턴이 있으면 그대로 둔다.
    """
    if not text:
        return ""

    if re.search(r"\d+\.\s", text):
        return text.strip()

    sents = [s.strip() for s in re.split(r"[\.!?]\s*", text) if s.strip()]
    if not sents:
        return text.strip()

    bullets = []
    for i, s in enumerate(sents[:max_n]):
        bullets.append(f"{i + 1}. {s}")

    return "\n".join(bullets).strip()


def extract_med_names(query: str) -> List[str]:
    """질문에서 약품명 후보만 추출."""
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

    disease = m.group(1)  # 예: '폐렴', 'HIV', 'HIV 감염'
    return disease.strip()


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
# QA JSONL 기반 RAG
# ───────────────────────────────
def retrieve_qa_pairs(
    query: str,
    k: int = 5,
    max_distance: float = 0.45,
    disease_kw: Optional[str] = None,
):
    """
    질환/건강 Q&A용 QAPair 벡터 검색.
    """
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

        def select_from_queryset(qs_base):
            cand = [it for it in qs_base if has_kw(it.question) or has_kw(it.answer)]
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
        if not kw_qs.exists():
            print(f"[QA-RAG] disease_kw='{disease_kw}' 포함된 QA 자체가 없음 → discard")
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
                f"[QA-RAG] disease_kw='{disease_kw}' 포함 QA는 있지만 "
                f"질문 앞부분에 메인 질환으로 쓰인 QA가 없음 → discard"
            )
            return []

        return primary_items

    return items


# ───────────────────────────────
# 질환별 증상용 QA 선택 헬퍼
# ───────────────────────────────
def find_symptom_qa(disease_kw: str, base_items: List[QAPair]) -> Optional[QAPair]:
    """
    disease_kw에 대해 answer 안에 '증상'이 들어간 QAPair를 우선 선택.
    """
    norm_kw = _normalize_ko_text(disease_kw)

    def has_disease(txt: Optional[str]) -> bool:
        if not txt:
            return False
        return norm_kw in _normalize_ko_text(txt)

    for it in base_items:
        ans = it.answer or ""
        if "증상" in ans and (has_disease(it.question) or has_disease(it.answer)):
            return it

    qs = QAPair.objects.filter(
        Q(question__icontains=disease_kw) | Q(answer__icontains=disease_kw),
        answer__icontains="증상",
    )
    if qs.exists():
        return qs.first()

    return None


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
    """'효능효과:', '용법용량:' 같은 라벨 이후만 잘라냄."""
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
    """
    특정 약(med)에 대해 원하는 섹션에 가장 잘 맞는 Chunk 하나 선택.
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
    """
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
    """
    - drug Chunk가 없으면: 일반 질의용 LLM
    - drug Chunk가 있으면: RAG + LLM
    """
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

    # 0. 질환/건강 일반 질문
    if intent == INTENT_GENERAL:
        disease_kw = extract_disease_keyword(question)
        print("disease_kw =", disease_kw)

        if disease_kw:
            q_norm = question.replace(" ", "")

            # 0-1. '증상' 질문이면 bullet 형태 강제
            if "증상" in q_norm:
                qa_items = retrieve_qa_pairs(
                    query=question,
                    k=3,
                    max_distance=0.45,
                    disease_kw=disease_kw,
                )

                symptom_text = ""

                if qa_items:
                    symptom_qa = find_symptom_qa(disease_kw, qa_items)

                    if symptom_qa:
                        raw_answer = (symptom_qa.answer or "").strip()
                        raw_answer = dedupe_sentences(raw_answer)

                        print(
                            "[QA-RAG] symptom_branch, use QAPair id=",
                            getattr(symptom_qa, "id", None),
                        )

                        symptom_sents: List[str] = []
                        seen = set()
                        for sent in re.split(r"[\.!?]\s*", raw_answer):
                            s = sent.strip()
                            if "증상" in s and s and s not in seen:
                                seen.add(s)
                                symptom_sents.append(s)

                        symptom_text = "\n".join(symptom_sents).strip()
                        print(
                            "[QA-RAG] symptom_branch, extracted_symptom_text =",
                            bool(symptom_text),
                        )

                if symptom_text:
                    instruction = f"""
아래는 '{disease_kw}'에 대한 의료 정보 중 '증상'이 언급된 부분입니다.

[증상 관련 원문]
{symptom_text}

[지시]
위 텍스트에서 '{disease_kw}'의 '증상'만 골라서
핵심 증상들을 한국어 bullet 형식(1. 2. 3. ...)으로 간단히 정리해라.
다른 내용(원인, 진단, 검사, 치료, 예방)은 포함하지 마라.

[답변]
"""
                    result = generate_answer(instruction).strip()
                    result = clean_output(result)
                    return force_numbered_bullets(result)

                instruction = f"""
너는 의료 정보를 설명하는 한국어 상담 어시스턴트이다.

[질환명]
{disease_kw}

[지시]
위 질환에 대해, 일반적으로 알려진 '대표 증상'만 한국어 bullet 형식(1. 2. 3. ...)으로 정리해라.

- 핵심 증상 위주로 4~6개 bullet로 정리해라.
- '원인, 진단, 검사, 치료, 예방' 내용은 절대 포함하지 마라.
- 확실하지 않은 것은 적지 말고, 필요한 경우에는 의사 진료를 권고해라.

[답변]
"""
                print("[QA-RAG] symptom_branch, fallback to LLM by disease name only")
                result = generate_answer(instruction).strip()
                result = clean_output(result)
                return force_numbered_bullets(result)

            # 0-2. '증상'이 아닌 질환 질문일 때만 QAPair answer 그대로 사용
            qa_items = retrieve_qa_pairs(
                query=question,
                k=3,
                max_distance=0.45,
                disease_kw=disease_kw,
            )
            if qa_items:
                top = qa_items[0]
                raw_answer = (top.answer or "").strip()
                raw_answer = dedupe_sentences(raw_answer)
                print("[QA-RAG] hit → QAPair.answer 사용 (non-symptom)")
                return clean_output(raw_answer)

        # disease_kw 없거나 QA 실패 → 아래 intent 분기로 계속 진행

    # 1. 비교 질문 (타이레놀 vs 판콜)
    if intent == INTENT_COMPARE:
        meds = detect_compare_intent(question) or []
        if len(meds) >= 2:
            return clean_output(build_compare_answer(question, meds))

    # 2. 증상 기반 약 추천 (증상 → 어떤 약?)
    if intent == INTENT_SYMPTOM:
        recs = recommend_by_symptom(question)
        return clean_output(build_symptom_answer(question, recs))

    # 3. 단일 약 질문들
    if intent == INTENT_SIDE_EFFECT:
        if chunks:
            return clean_output(build_side_effect_answer(question, chunks))
        return clean_output(build_general_answer(question, chunks))

    if intent == INTENT_EFFICACY:
        if chunks:
            return clean_output(build_efficacy_answer(question, chunks))
        return clean_output(build_general_answer(question, chunks))

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

    # 4. 나머지 → 일반 LLM
    return clean_output(build_general_answer(question, chunks))