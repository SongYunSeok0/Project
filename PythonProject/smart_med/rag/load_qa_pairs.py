# rag/load_qa_pairs.py
import json
import uuid
from pathlib import Path

from django.db import transaction

from rag.models import QAPair
from rag.embeddings import get_embedding

# 네가 실제 파일 둔 위치
JSONL_PATH = Path(r"C:\Users\user\Desktop\qwen_sft\data\qa_pairs_utf8.jsonl")

BATCH_SIZE = 256  # 한 번에 bulk_create 할 개수


def load_qa_pairs():
    if not JSONL_PATH.exists():
        print("ERROR: JSONL 파일 없음:", JSONL_PATH)
        return

    print("Loading JSONL:", JSONL_PATH)

    # 기존 데이터 전부 삭제(처음부터 다시 적재)
    QAPair.objects.all().delete()

    total = 0
    batch = []

    with JSONL_PATH.open("r", encoding="utf-8") as f:
        for line_no, line in enumerate(f, start=1):
            line = line.strip()
            if not line:
                continue

            try:
                obj = json.loads(line)
            except json.JSONDecodeError:
                print(f"[WARN] line {line_no}: JSON 파싱 실패")
                continue

            instr = (obj.get("instruction") or "").strip()
            out = (obj.get("output") or "").strip()
            cat = (obj.get("category") or "").strip()

            if not instr or not out:
                continue

            # 질문 기준 임베딩
            emb = get_embedding(instr)

            qa_id = f"{line_no}:{uuid.uuid4().hex}"

            batch.append(
                QAPair(
                    qa_id=qa_id,
                    question=instr,
                    answer=out,
                    category=cat,
                    embedding=emb,
                )
            )

            if len(batch) >= BATCH_SIZE:
                with transaction.atomic():
                    QAPair.objects.bulk_create(batch)
                total += len(batch)
                print(f"saved {total} rows...")
                batch = []

    if batch:
        with transaction.atomic():
            QAPair.objects.bulk_create(batch)
        total += len(batch)

    print("저장 완료:", total)
