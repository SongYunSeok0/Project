# rag/utils.py
import re
from typing import List
from .constants import (
    MED_NAME_HINT, 
    MEDICAL_KEYWORDS, 
    NON_MEDICAL_KEYWORDS,
    GREETING_KEYWORDS,
    HEALTH_FOOD_KEYS,
)


def normalize(s: str) -> str:
    """공백 제거 정규화"""
    return s.replace(" ", "").strip()


def dedupe_sentences(text: str) -> str:
    """중복 문장 제거"""
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
    """출력 텍스트 정리"""
    if not text:
        return ""

    t = text.strip()

    # 절반 중복 제거
    n = len(t)
    mid = n // 2
    if n % 2 == 0 and t[:mid] == t[mid:]:
        t = t[:mid].strip()

    # 중복 단락 제거
    paras = []
    seen_paras = set()
    for p in re.split(r"\n{2,}", t):
        p2 = p.strip()
        if not p2:
            continue
        if p2 in seen_paras:
            continue
        paras.append(p2)
        seen_paras.add(p2)
    t = "\n\n".join(paras)

    # 중복 문장 제거
    t = dedupe_sentences(t)
    t = t.replace("\ufffd", "")

    return t.strip()


def extract_med_names(query: str) -> List[str]:
    """질문에서 약품명 추출"""
    toks = re.split(r"[^\w가-힣]+", query)
    meds: List[str] = []
    for t in toks:
        if len(t) < 2:
            continue
        if any(h in t for h in MED_NAME_HINT):
            meds.append(t)
    return list(dict.fromkeys(meds))


def short(s: str, n: int = 200) -> str:
    """텍스트 짧게 자르기"""
    s = (s or "").replace("\n", " ").strip()
    return s[:n] + ("…" if len(s) > n else "") if s else ""


# ========== 인사말 감지 및 응답 ==========

def is_greeting(question: str) -> bool:
    """
    질문이 인사말인지 판단
    """
    q = question.strip().replace(" ", "").lower()
    
    # 짧은 문장만 인사로 간주 (30자 이하)
    if len(question.strip()) > 30:
        return False
    
    # 인사 패턴 체크
    greeting_patterns = [
        "안녕",
        "반가",
        "처음",
        "하이",
        "헬로",
        "좋은아침",
        "좋은하루",
    ]
    
    for pattern in greeting_patterns:
        if pattern in q:
            return True
    
    return False


def get_greeting_response() -> str:
    """인사말에 대한 응답"""
    return (
        "안녕하세요! 😊\n\n"
        "저는 의약품과 건강 정보를 안내하는 AI 어시스턴트입니다.\n\n"
        "다음과 같은 질문에 도움을 드릴 수 있습니다:\n"
        "• 약물의 효능, 용법, 부작용 정보\n"
        "• 증상에 따른 약 추천\n"
        "• 약물 상호작용 및 주의사항\n"
        "• 건강기능식품 정보\n\n"
        "궁금하신 점을 편하게 물어보세요!"
    )


# ========== 의료 질문 필터링 ==========

def is_medical_question(question: str) -> bool:
    """
    질문이 의료/건강 관련인지 판단
    
    Returns:
        True: 의료 관련 질문
        False: 비의료 질문
    """
    q_norm = normalize(question)
    
    # 0. 인사말은 의료 질문으로 간주 (별도 처리 위해)
    if is_greeting(question):
        return True
    
    # 1. 명백한 비의료 키워드가 있으면 False
    if any(k in q_norm for k in NON_MEDICAL_KEYWORDS):
        return False
    
    # 2. 의료 키워드가 하나라도 있으면 True
    if any(k in q_norm for k in MEDICAL_KEYWORDS):
        return True
    
    # 3. 증상 키워드가 있으면 True
    try:
        from .symptom import SYMPTOM_KEYWORDS
        if any(k in q_norm for k in SYMPTOM_KEYWORDS):
            return True
    except ImportError:
        pass
    
    # 4. 약품명 힌트가 있으면 True
    if any(h in question for h in MED_NAME_HINT):
        return True
    
    # 5. 건강기능식품 키워드가 있으면 True
    if any(h in q_norm for h in HEALTH_FOOD_KEYS):
        return True
    
    # 6. 그 외는 False (보수적으로 차단)
    return False


def get_non_medical_response() -> str:
    """비의료 질문에 대한 거부 메시지"""
    return (
        "죄송합니다. 저는 의약품과 건강 정보에 특화된 상담 서비스입니다.\n\n"
        "다음과 같은 질문에 답변할 수 있습니다:\n"
        "• 약물의 효능, 용법, 부작용\n"
        "• 증상에 따른 약 추천\n"
        "• 약물 상호작용 및 주의사항\n"
        "• 건강기능식품 정보\n\n"
        "의료/건강 관련 질문을 입력해주세요."
    )