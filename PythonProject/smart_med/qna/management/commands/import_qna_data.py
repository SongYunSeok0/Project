from django.core.management.base import BaseCommand
from qna.models import QnA
import os, json

class Command(BaseCommand):
    help = "루트 폴더 내 모든 하위폴더의 QnA JSON 데이터를 DB에 삽입합니다."

    def add_arguments(self, parser):
        parser.add_argument("root_folder", type=str, help="최상위 폴더 경로")

    def handle(self, *args, **kwargs):
        root_folder = kwargs["root_folder"]
        if not os.path.exists(root_folder):
            self.stdout.write(self.style.ERROR(f"❌ 폴더를 찾을 수 없습니다: {root_folder}"))
            return

        total_count = 0
        for dirpath, _, filenames in os.walk(root_folder):  # 모든 하위 폴더 탐색
            for filename in filenames:
                if filename.endswith(".json"):
                    file_path = os.path.join(dirpath, filename)
                    try:
                        with open(file_path, "r", encoding="utf-8") as f:
                            data = json.load(f)

                        # key 이름 유연하게 처리
                        question = data.get("질문") or data.get("question") or data.get("Question")
                        answer = data.get("답변") or data.get("answer") or data.get("Answer")
                        category = os.path.basename(os.path.dirname(file_path))  # 상위 폴더명으로 분류 자동 지정

                        if question and answer:
                            QnA.objects.update_or_create(
                                question=question.strip(),
                                defaults={
                                    "answer": answer.strip(),
                                    "category": category.strip() if category else "",
                                }
                            )
                            total_count += 1

                    except Exception as e:
                        self.stdout.write(self.style.WARNING(f"⚠️ {filename} 처리 중 오류: {e}"))

        self.stdout.write(self.style.SUCCESS(f"✅ 총 {total_count}개의 QnA 데이터 삽입 완료!"))
