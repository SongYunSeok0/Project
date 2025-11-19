# rag/intents.py
import re
from typing import List, Optional

from .models import Chunk


def _normalize(s: str) -> str:
    return s.replace(" ", "").strip()


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


def _pick_target_chunk(
    question: str,
    chunks: List[Chunk],
    prefer_sections: Optional[List[str]] = None,
) -> Optional[Chunk]:
    """질문에 포함된 약 이름 + 섹션 우선순위로 1개 선택."""
    if not chunks:
        return None

    q = _normalize(question)

    # 1) 질문에 약 이름 일부가 들어간 경우 우선
    for c in chunks:
        name_norm = _normalize(c.item_name or "")
        for tok in re.split(r"[()\s]", name_norm):
            tok = tok.strip()
            if tok and tok in q:
                return c

    # 2) 섹션 우선
    if prefer_sections:
        for sec in prefer_sections:
            for c in chunks:
                if c.section and sec in c.section:
                    return c

    # 3) fallback
    return chunks[0]


def build_side_effect_answer(question: str, chunks: List[Chunk]) -> str:
    c = _pick_target_chunk(
        question,
        chunks,
        prefer_sections=["부작용", "이상반응"],
    )
    if not c:
        return "참고 문서에서 해당 약의 부작용 정보를 찾지 못했습니다."

    text = c.text or ""
    core = _extract_after_label(text, ["부작용", "이상반응"])

    parts = [p.strip() for p in re.split(r"[,，\n]", core) if p.strip()]

    main = parts[:6]

    cautions = [
        p
        for p in parts
        if any(k in p for k in ["중지", "악화", "지속", "새로운 증상", "의사", "약사"])
    ][:3]

    lines: List[str] = []

    lines.append("1. 주요 부작용")
    if main:
        for p in main:
            lines.append(f"- {p}")
    else:
        lines.append("- 부작용 정보를 추출할 수 없습니다.")

    lines.append("")
    lines.append("2. 복용 시 주의사항")
    if cautions:
        for p in cautions:
            lines.append(f"- {p}")
    else:
        lines.append(
            "- 통증이나 발열 등이 지속되거나 악화되거나, 새로운 증상이 나타나면 "
            "복용을 중지하고 의사 또는 약사와 상의하십시오."
        )

    return "\n".join(lines)


def build_efficacy_answer(question: str, chunks: List[Chunk]) -> str:
    c = _pick_target_chunk(
        question,
        chunks,
        prefer_sections=["효능효과", "효능"],
    )
    if not c:
        return "참고 문서에서 해당 약의 효능·효과 정보를 찾지 못했습니다."

    text = c.text or ""
    core = _extract_after_label(text, ["효능효과", "효능"])
    core = core.replace("\n", " ").strip()

    return "1. 효능·효과\n- " + (core or "효능·효과 정보를 추출할 수 없습니다.")


def build_dosage_answer(question: str, chunks: List[Chunk]) -> str:
    c = _pick_target_chunk(
        question,
        chunks,
        prefer_sections=["용법용량"],
    )
    if not c:
        return "참고 문서에서 해당 약의 용법·용량 정보를 찾지 못했습니다."

    text = c.text or ""
    core = _extract_after_label(text, ["용법용량"])
    core = core.replace("\n", " ").strip()

    return "1. 용법·용량 요약\n- " + (core or "용법·용량 정보를 추출할 수 없습니다.")


def build_interaction_answer(question: str, chunks: List[Chunk]) -> str:
    c = _pick_target_chunk(
        question,
        chunks,
        prefer_sections=["상호작용"],
    )
    if not c:
        return "참고 문서에서 해당 약의 약물 상호작용 정보를 찾지 못했습니다."

    text = c.text or ""
    core = _extract_after_label(text, ["상호작용"])
    core = core.replace("\n", " ").strip()

    return "1. 약물 상호작용 요약\n- " + (core or "상호작용 정보를 추출할 수 없습니다.")


def build_warning_answer(question: str, chunks: List[Chunk]) -> str:
    c = _pick_target_chunk(
        question,
        chunks,
        prefer_sections=["사용상 주의사항", "주의사항", "주의", "경고"],
    )
    if not c:
        return "참고 문서에서 해당 약의 주의사항·경고 정보를 찾지 못했습니다."

    text = c.text or ""
    core = _extract_after_label(text, ["사용상 주의사항", "주의사항", "주의", "경고"])
    core = core.replace("\n", " ").strip()

    return "1. 주의사항·경고 요약\n- " + (core or "주의사항·경고 정보를 추출할 수 없습니다.")
