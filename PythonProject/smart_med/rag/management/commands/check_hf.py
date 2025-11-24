# rag/management/commands/check_hf_fatigue.py
from django.core.management.base import BaseCommand
from rag.models import Chunk


FATIGUE_KEYWORDS = [
    "피로", "피로감", "피곤", "피로 회복", "피로회복",
    "눈의 피로", "피로도",
]


def contains_fatigue_kw(text: str) -> bool:
    if not text:
        return False
    t = text.replace(" ", "")
    for kw in FATIGUE_KEYWORDS:
        if kw.replace(" ", "") in t:
            return True
    return False


class Command(BaseCommand):
    help = "hf_function 섹션에서 '피로' 관련 기능성이 포함된 제품 목록을 출력합니다."

    def handle(self, *args, **options):
        qs = (
            Chunk.objects
            .filter(section__startswith="hf_function")
            .order_by("item_name", "chunk_index")
        )

        products = {}  # item_name -> {"has_fatigue": bool, "texts": [str]}
        for c in qs:
            name = (c.item_name or "제품명 미상").strip()
            info = products.setdefault(name, {"has_fatigue": False, "texts": []})

            text = (c.text or "").strip()
            info["texts"].append(text)

            if contains_fatigue_kw(text):
                info["has_fatigue"] = True

        total = len(products)
        with_fatigue = sum(1 for v in products.values() if v["has_fatigue"])
        without_fatigue = total - with_fatigue

        self.stdout.write("")
        self.stdout.write(f"총 제품 수: {total}")
        self.stdout.write(f"피로 관련 기능성 포함: {with_fatigue}")
        self.stdout.write(f"피로 관련 기능성 없음: {without_fatigue}")
        self.stdout.write("")

        self.stdout.write("=== 피로 관련 기능성 있는 제품 ===")
        for name, info in products.items():
            if not info["has_fatigue"]:
                continue
            sample = " ".join(info["texts"])[:200].replace("\n", " ")
            self.stdout.write(f"- {name}: {sample}")

        self.stdout.write("")
        self.stdout.write("=== 피로 관련 기능성 없는 제품 ===")
        for name, info in products.items():
            if info["has_fatigue"]:
                continue
            sample = " ".join(info["texts"])[:200].replace("\n", " ")
            self.stdout.write(f"- {name}: {sample}")
