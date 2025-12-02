# rag/intent_detector.py
from .constants import (
    INTENT_SIDE_EFFECT,
    INTENT_EFFICACY,
    INTENT_DOSAGE,
    INTENT_INTERACTION,
    INTENT_WARNING,
    INTENT_SYMPTOM,
    INTENT_GENERAL,
    SIDE_EFFECT_KEYS,
    EFFICACY_KEYS,
    DOSAGE_KEYS,
    INTERACTION_KEYS,
    WARNING_KEYS,
    HEALTH_FOOD_KEYS,
    FOLLOWUP_INCLUDE_KEYS,
)
from .utils import normalize
from .symptom import SYMPTOM_KEYWORDS


def detect_intent(question: str) -> str:
    q = normalize(question)

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

    if any(h in q for h in HEALTH_FOOD_KEYS):
        return INTENT_EFFICACY

    if any(sym in q for sym in SYMPTOM_KEYWORDS) and any(
        k in q for k in FOLLOWUP_INCLUDE_KEYS
    ):
        return INTENT_EFFICACY

    if any(sym in q for sym in SYMPTOM_KEYWORDS):
        return INTENT_SYMPTOM

    return INTENT_GENERAL