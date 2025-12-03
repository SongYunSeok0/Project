# rag/constants.py
# Intent 정의
INTENT_SIDE_EFFECT = "side_effect"
INTENT_EFFICACY = "efficacy"
INTENT_DOSAGE = "dosage"
INTENT_INTERACTION = "interaction"
INTENT_WARNING = "warning"
INTENT_SYMPTOM = "symptom"
INTENT_GENERAL = "general"

# 키워드 정의
SIDE_EFFECT_KEYS = ["부작용", "이상반응"]
EFFICACY_KEYS = ["효능", "효과", "어디에좋아", "무엇에좋아", "어디에 좋", "무엇에 좋"]

DOSAGE_KEYS = [
    "용법",
    "용량",
    "복용법",
    "먹는법",
    "하루몇번",
    "하루 몇 번",
    "몇번",
    "몇 번",
    "몇회",
    "몇 회",
    "언제먹",
    "언제 먹",
    "언제 먹어",
    "언제 복용",
]

INTERACTION_KEYS = ["상호작용", "같이먹어도", "같이 먹어도", "병용", "함께복용", "함께 복용"]
WARNING_KEYS = ["주의사항", "주의", "경고", "사용상 주의사항"]

MED_NAME_HINT = ["정", "캡슐", "액", "시럽", "산", "펜", "콜", "타이레놀", "판콜", "콜드"]

FOLLOWUP_INCLUDE_KEYS = [
    "포함돼",
    "포함되",
    "포함안돼",
    "포함안되",
    "해당돼",
    "해당되",
    "들어가",
    "들어있",
    "들어가있",
]

HEALTH_FOOD_KEYS = [
    "영양제",
    "건강기능식품",
    "건기식",
    "비타민",
    "오메가3",
    "유산균",
    "프로바이오틱스",
]

MAX_CHUNK_CHARS = 600