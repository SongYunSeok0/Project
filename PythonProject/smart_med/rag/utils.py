# rag/utils.py
import re
from typing import List
from .constants import MED_NAME_HINT


def normalize(s: str) -> str:
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
        paras.append(p2)
        seen_paras.add(p2)
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


def short(s: str, n: int = 200) -> str:
    s = (s or "").replace("\n", " ").strip()
    return s[:n] + ("…" if len(s) > n else "") if s else ""