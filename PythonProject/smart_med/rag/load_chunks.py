# rag/load_chunks.py
import json
import uuid
from pathlib import Path

from rag.models import Chunk
from rag.embeddings import get_embedding  # EMB_DIM=384 벡터 리턴

# manage.py 기준 경로
JSON_PATH = Path("data/drb_easy_drug.json")
MAX_CHARS = 800  # 한 chunk 최대 길이


def _add_chunks(item_name: str, section_name: str, full_text: str, start_index: int, bulk: list) -> int:
    """
    긴 텍스트를 MAX_CHARS 단위로 잘라서 여러 Chunk 생성.
    start_index: 이 약에서 시작할 chunk_index
    return: 다음에 쓸 chunk_index
    """
    text = (full_text or "").strip()
    if not text:
        return start_index

    idx = start_index

    for start in range(0, len(text), MAX_CHARS):
        chunk_text = text[start : start + MAX_CHARS]

        # 384차원 임베딩 생성
        emb = get_embedding(chunk_text)

        # ★ chunk_id는 짧게(uuid만 사용) → max_length=128 안에 항상 들어감
        chunk_id = uuid.uuid4().hex  # 32자

        bulk.append(
            Chunk(
                chunk_id=chunk_id,
                item_name=item_name,
                section=section_name,
                chunk_index=idx,
                text=chunk_text,
                embedding=emb,
            )
        )
        idx += 1

    return idx


def load_chunks():
    if not JSON_PATH.exists():
        print("ERROR: JSON 파일 없음:", JSON_PATH)
        return

    data = json.loads(JSON_PATH.read_text(encoding="utf-8"))
    print("총 JSON 개수:", len(data))

    # 기존 Chunk 모두 삭제
    Chunk.objects.all().delete()

    bulk: list[Chunk] = []

    for item in data:
        name = (item.get("ITEM_NAME") or "").strip()
        if not name:
            continue

        # 네 JSON 키 구조에 맞게 매핑
        eff = item.get("EFCY_QESITM")         # 효능·효과
        dose = item.get("USE_METHOD_QESITM")  # 용법·용량
        warn1 = item.get("ATPN_WARN_QESITM")  # 경고
        warn2 = item.get("ATPN_QESITM")       # 일반 주의
        intrc = item.get("INTRC_QESITM")      # 상호작용
        se = item.get("SE_QESITM")            # 부작용

        idx = 0

        if eff:
            idx = _add_chunks(name, "효능효과", eff, idx, bulk)
        if dose:
            idx = _add_chunks(name, "용법용량", dose, idx, bulk)
        if se:
            idx = _add_chunks(name, "부작용", se, idx, bulk)
        if warn1:
            idx = _add_chunks(name, "사용상 주의사항", warn1, idx, bulk)
        if warn2:
            idx = _add_chunks(name, "사용상 주의사항", warn2, idx, bulk)
        if intrc:
            idx = _add_chunks(name, "상호작용", intrc, idx, bulk)

    # 한 번에 insert
    Chunk.objects.bulk_create(bulk)
    print("저장 완료:", len(bulk))