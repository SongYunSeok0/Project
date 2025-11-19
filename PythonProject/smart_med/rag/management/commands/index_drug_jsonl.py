# smart_med/rag/management/commands/index_drug_jsonl.py
from django.core.management.base import BaseCommand
from django.db import transaction
from sentence_transformers import SentenceTransformer
from rag.models import Chunk
from pathlib import Path
import ujson as json

SECTION_MAP = {
    "EFCY_QESITM": "효능효과",
    "USE_METHOD_QESITM": "용법용량",
    "ATPN_WARN_QESITM": "사용상 주의사항(경고)",
    "ATPN_QESITM": "사용상 주의사항",
    "INTRC_QESITM": "상호작용",
    "SE_QESITM": "부작용",
}

def split_paragraphs(text: str, max_chars: int = 1000):
    if not text:
        return []
    text = text.replace("\\n", "\n")
    parts, buf = [], ""
    for line in text.splitlines():
        line = line.strip()
        if not line:
            continue
        if len(buf) + len(line) + 1 <= max_chars:
            buf = (buf + " " + line).strip()
        else:
            if buf: parts.append(buf)
            buf = line
    if buf: parts.append(buf)

    final = []
    for p in parts:
        if len(p) <= max_chars:
            final.append(p)
        else:
            seg = ""
            for sent in p.replace("다.", "다.\n").split("\n"):
                sent = sent.strip()
                if not sent:
                    continue
                if len(seg) + len(sent) + 1 <= max_chars:
                    seg = (seg + " " + sent).strip()
                else:
                    if seg: final.append(seg)
                    seg = sent
            if seg: final.append(seg)
    return final

class Command(BaseCommand):
    help = "drug JSON/JSONL → pgvector 인덱싱 (원본 컬럼에서 섹션별 청크 생성 지원)"

    def add_arguments(self, parser):
        parser.add_argument("--path", required=True, help="drb_easy_drug.json 또는 .jsonl 경로")
        parser.add_argument("--batch", type=int, default=512)

    def handle(self, *args, **opts):
        path = Path(opts["path"])
        if not path.exists():
            self.stderr.write(f"파일을 찾을 수 없습니다: {path}")
            return

        # 1) 파일 로드: jsonl 또는 json 배열 모두 지원
        records = []
        if path.suffix.lower() == ".jsonl":
            with path.open("r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if not line: continue
                    records.append(json.loads(line))
        else:
            data = json.load(path.open("r", encoding="utf-8"))
            if isinstance(data, list):
                records = data
            else:
                self.stderr.write("JSON 최상단이 리스트가 아님")
                return

        # 2) 입력 형태 판별
        #  - 사전 청크(jsonl 스타일): 각 항목이 text 필드를 가짐
        #  - 원본 컬럼(csv→json): ITEM_NAME, EFCY_QESITM 등
        prechunked = bool(records and isinstance(records[0], dict) and "text" in records[0])

        # 3) 사전 청크면 그대로 사용, 아니면 여기서 섹션별 청크 생성
        items = []
        if prechunked:
            # 기대 스키마: id, item_name, section, chunk_index, text
            items = records
        else:
            # 기대 스키마: ITEM_NAME와 각 섹션 컬럼이 존재
            doc_id = 0
            for row in records:
                name = str(row.get("ITEM_NAME", "")).strip()
                if not name:
                    continue
                for col, sec in SECTION_MAP.items():
                    raw = str(row.get(col, "") or "").strip()
                    if not raw:
                        continue
                    chunks = split_paragraphs(raw, max_chars=1000)
                    for i, ch in enumerate(chunks):
                        items.append({
                            "id": f"d{doc_id}",
                            "item_name": name,
                            "section": sec,
                            "chunk_index": i,
                            "text": f"[{name}] {sec}: {ch}"
                        })
                        doc_id += 1

        self.stdout.write(f"{len(items)}개의 청크 준비됨, 임베딩 시작...")

        # 4) 임베딩 + DB 적재
        model = SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")
        B = opts["batch"]
        for i in range(0, len(items), B):
            batch = items[i:i+B]
            texts = [r["text"] for r in batch]
            embs = model.encode(texts, batch_size=B, normalize_embeddings=True)

            with transaction.atomic():
                objs = []
                for r, e in zip(batch, embs):
                    objs.append(Chunk(
                        chunk_id=r.get("id") or f"auto_{i}",
                        item_name=r["item_name"],
                        section=r["section"],
                        chunk_index=r.get("chunk_index", 0),
                        text=r["text"],
                        embedding=e.tolist(),
                    ))
                Chunk.objects.bulk_create(objs, ignore_conflicts=True)

            self.stdout.write(f"{i + len(batch)}/{len(items)} 인덱싱 완료")

        self.stdout.write(self.style.SUCCESS("✅ 모든 청크 인덱싱 완료"))
