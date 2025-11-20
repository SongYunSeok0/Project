# rag/symptom.py
import re
from typing import List, Dict, Any

from django.db.models import Q

from .models import Chunk


from typing import Dict, List

SYMPTOM_CATEGORY_MAP: Dict[str, List[str]] = {
    "두통": ["두통", "해열진통", "진통", "머리아픔", "머리아파", "머리가 아픔", "머리가아파"],
    "기침": ["기침", "진해", "거담", "감기"],
    "콧물": ["콧물", "비염", "감기"],
    "코막힘": ["코막힘", "비충혈", "비염"],
    "열": ["해열", "발열", "해열진통", "열남", "열나요"],
    "발열": ["해열", "발열", "해열진통", "열남", "열나요"],
    "근육통": ["근육통", "근육통증", "해열진통", "몸살"],
    "감기": ["감기", "감기증상", "해열진통", "진해거담"],
    "목아픔": ["인후통", "목통증", "목아픔", "목아파"],
    "몸살": ["감기", "몸살", "근육통", "온몸아파"],
    "매스꺼움": ["구역", "오심", "구토", "매스꺼움", "메스꺼움"],
    "메스꺼움": ["구역", "오심", "구토", "메스꺼움", "매스꺼움"],
    "구토": ["구토", "오심", "토함", "토할것같아"],
    "복통": ["복통", "뱃아픔", "배아파", "위장관", "소화불량", "배가아파"],
    "속쓰림": ["속쓰림", "속이쓰림", "명치쓰림", "위염", "속이쓰리고", "속이쓰려요", "속이쓰리고"],
    "소화불량": ["소화불량", "체함", "식체"],
    "생리통": ["생리통", "월경통", "진통", "해열진통", "하복통"],
    "월경통": ["월경통", "생리통", "진통", "해열진통", "하복통"],
}

# key + value 전부를 증상 키워드로 사용
SYMPTOM_KEYWORDS: List[str] = []
for base, words in SYMPTOM_CATEGORY_MAP.items():
    SYMPTOM_KEYWORDS.append(base)
    SYMPTOM_KEYWORDS.extend(words)
# 중복 제거
SYMPTOM_KEYWORDS = list(dict.fromkeys(SYMPTOM_KEYWORDS))

CHILD_HINTS = ["아이", "어린이", "소아", "키즈", "초등학생"]
ADULT_HINTS = ["성인", "어른", "어른용", "성인용"]

SYMPTOM_STOPWORDS = [
    "약", "약이", "약을", "어떤", "무슨", "어느", "좋을까", "좋을까요",
    "추천", "먹어야", "먹을까", "먹으면", "골라", "골라줘", "정해줘",
    "있는데", "인데", "있어", "나는", "제가", "좀", "요", "입니다", "하는데",
    "무슨약", "어떤약", "약좀", "약은",
]


def _normalize(s: str) -> str:
    return s.replace(" ", "").strip()


def extract_symptoms(question: str) -> List[str]:
    """문장에서 증상 후보 단어 추출 → 항상 카테고리 키(두통, 속쓰림 등)로 반환."""
    # 한글 + 공백만 남기기
    q_clean = re.sub(r"[^가-힣\s]", " ", question)
    toks = [t.strip() for t in q_clean.split() if len(t.strip()) >= 2]
    toks = [t for t in toks if t not in SYMPTOM_STOPWORDS]

    # 공백 제거 버전(문장 전체)
    q_norm = _normalize(q_clean)

    found = set()

    # 1) 문장 전체에서 base/동의어 매칭 → base(키값)만 모음
    for base, words in SYMPTOM_CATEGORY_MAP.items():
        # base 직접 포함
        if base in q_norm:
            found.add(base)
            continue

        # 동의어들 포함
        for w in words:
            if w and w in q_norm:
                found.add(base)
                break

    if found:
        # 항상 "속쓰림", "두통" 같은 카테고리 이름만 반환
        return list(found)

    # 2) 매핑에 없는 증상(새 단어)일 때만 토큰 그대로 fallback
    return toks


def detect_age_group(question: str) -> str:
    """
    질문에서 어린이/성인 추정
    return: "child" / "adult" / "unknown"
    """
    q = _normalize(question)
    if any(h in q for h in CHILD_HINTS):
        return "child"
    if any(h in q for h in ADULT_HINTS):
        return "adult"

    m = re.search(r"(\d+)\s*살", question)
    if m:
        try:
            age = int(m.group(1))
            if age < 12:
                return "child"
            return "adult"
        except ValueError:
            pass
    return "unknown"


def classify_product_age(item_name: str) -> str:
    """제품명이 어린이/성인용인지 추정: 'child' / 'adult' / 'unknown'."""
    name = _normalize(item_name or "")
    if any(k in name for k in ["어린이", "소아", "키즈", "베이비", "키드"]):
        return "child"
    if any(k in name for k in ["성인", "어른용", "성인용"]):
        return "adult"
    return "unknown"


def _score_chunk_for_symptoms(text_norm: str, symptoms: List[str]) -> (int, set):
    """
    한 Chunk에 대해:
    - 어떤 증상들이 매칭됐는지
    - 점수는 얼마나 되는지 계산
    """
    score = 0
    matched = set()

    for s in symptoms:
        matched_flag = False

        # 증상 단어 직접 포함
        if s in text_norm:
            score += 2
            matched_flag = True

        # 매핑된 카테고리 단어 포함
        for cat in SYMPTOM_CATEGORY_MAP.get(s, []):
            if cat in text_norm:
                score += 1
                matched_flag = True

        if matched_flag:
            matched.add(s)

    return score, matched


def recommend_by_symptom(question: str, topn: int = 5) -> List[Dict[str, Any]]:
    """
    증상 기반 약 추천:
    - 효능/효과 섹션에서 증상/카테고리 단어 검색
    - 질문에 나온 여러 증상을 동시에 평가
      1) 우선: 모든 증상을 다 포함하는 약만(strict)
      2) 그런 약이 없으면: 일부 증상만 포함하는 약도 허용(loose)
    - 어린이/성인 여부 반영
    - 동일 item_name 중 최고 점수만 사용
    """
    symptoms = extract_symptoms(question)
    if not symptoms:
        return []

    age_group = detect_age_group(question)

    qs = Chunk.objects.filter(
        Q(section__icontains="효능효과") | Q(section__icontains="효능")
    )

    strict_best: Dict[str, Dict[str, Any]] = {}  # 모든 증상 포함
    loose_best: Dict[str, Dict[str, Any]] = {}   # 일부만 포함

    for c in qs:
        text_norm = _normalize((c.text or "") + " " + (c.item_name or ""))

        base_score, matched = _score_chunk_for_symptoms(text_norm, symptoms)
        if base_score <= 0:
            continue

        # 연령대 보정
        prod_age = classify_product_age(c.item_name or "")
        age_tag = ""
        score = base_score

        if age_group == "child":
            if prod_age == "child":
                score += 1
                age_tag = "어린이용"
            elif prod_age == "adult":
                score -= 1
        elif age_group == "adult":
            if prod_age == "adult":
                score += 1
                age_tag = "성인용"
            elif prod_age == "child":
                score -= 1
                age_tag = "어린이용"

        if score <= 0:
            continue

        key = c.item_name

        # loose: 증상 일부만 매칭돼도 포함
        if key not in loose_best or score > loose_best[key]["score"]:
            loose_best[key] = {
                "score": score,
                "chunk": c,
                "age_tag": age_tag,
                "matched": matched,
            }

        # strict: 모든 증상을 다 매칭한 경우만
        if len(matched) == len(symptoms):
            if key not in strict_best or score > strict_best[key]["score"]:
                strict_best[key] = {
                    "score": score,
                    "chunk": c,
                    "age_tag": age_tag,
                    "matched": matched,
                }

    # 우선 strict 결과 사용, 없으면 loose 사용
    target = strict_best if strict_best else loose_best
    if not target:
        return []

    recs = sorted(target.values(), key=lambda x: -x["score"])[:topn]
    return recs


def build_symptom_answer(question: str, recs: List[Dict[str, Any]]) -> str:
    if not recs:
        return "참고 문서에서 해당 증상에 맞는 약을 찾지 못했습니다."

    symptoms = extract_symptoms(question)
    sym_line = ", ".join(symptoms) if symptoms else question

    lines: List[str] = []
    lines.append("1. 증상")
    lines.append(f"- {sym_line}")
    lines.append("")
    lines.append("2. 추천 약품")

    for r in recs:
        c: Chunk = r["chunk"]
        age_tag = r.get("age_tag") or ""
        tag_str = f" ({age_tag})" if age_tag else ""
        lines.append(f"- {c.item_name}{tag_str}")

    lines.append("")
    lines.append("3. 선택 기준 요약")
    lines.append("- 해당 약품들은 효능·효과에 질문한 증상(또는 관련 카테고리)이 포함되어 있습니다.")
    lines.append("- 실제 복용 전에는 반드시 의사 또는 약사와 상의해야 합니다.")

    return "\n".join(lines)
