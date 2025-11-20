# rag/load_healthfood.py

from pathlib import Path
import json
from django.db import transaction

from rag.models import HealthFood, Chunk
from rag.embeddings import get_embedding


BASE_DIR = Path(__file__).resolve().parent.parent
DEFAULT_PATH = BASE_DIR / "data" / "health_food_data.json"


def load_healthfood(json_path: str | Path = DEFAULT_PATH):
    """
    건강기능식품 JSON 파일을 읽어서
    - HealthFood 테이블 채우고
    - Chunk(hf_usage / hf_caution / hf_function) 생성
    """
    json_path = Path(json_path)

    if not json_path.exists():
        raise FileNotFoundError(f"JSON 파일 없음: {json_path}")

    with open(json_path, encoding="utf-8") as f:
        items = json.load(f)

    print(f"[LOAD] 총 {len(items)}개 제품 로드 시작")

    chunk_objs: list[Chunk] = []
    processed = 0

    # 같은 (제조사, 제품명) 중복 방지용
    processed_keys: set[tuple[str, str]] = set()

    with transaction.atomic():
        # 1) 기존 건강기능식품용 Chunk(hf_*) 전부 삭제
        deleted = Chunk.objects.filter(section__startswith="hf_").delete()
        print(f"[LOAD] 기존 hf_* Chunk 삭제: {deleted}")

        # 2) HealthFood upsert + Chunk 객체 메모리에 모으기
        for idx, item in enumerate(items):
            ent = (item.get("ENTRPS") or "").strip()
            name = (item.get("PRDUCT") or "").strip()
            srv_use = (item.get("SRV_USE") or "").strip()
            hint = (item.get("INTAKE_HINT1") or "").strip()
            fn = (item.get("MAIN_FNCTN") or "").strip()

            if not name:
                continue

            key = (ent, name)
            if key in processed_keys:
                # 같은 제조사/제품명은 한 번만 처리
                continue
            processed_keys.add(key)

            hf, _created = HealthFood.objects.update_or_create(
                manufacturer=ent,
                product_name=name,
                defaults={
                    "serve_use": srv_use or None,
                    "intake_hint": hint or None,
                    "main_function": fn or None,
                },
            )

            item_name = (
                f"{hf.manufacturer} - {hf.product_name}"
                if hf.manufacturer
                else hf.product_name
            )

            # usage chunk
            if srv_use:
                text = (
                    f"제품명: {hf.product_name}\n"
                    f"제조사: {hf.manufacturer}\n"
                    f"섭취 방법: {srv_use}"
                ).strip()
                emb = get_embedding(text)
                chunk_objs.append(
                    Chunk(
                        chunk_id=f"hf_{hf.id}_usage",
                        item_name=item_name,
                        section="hf_usage",
                        chunk_index=0,
                        text=text,
                        embedding=emb,
                    )
                )

            # caution chunk
            if hint:
                text = (
                    f"제품명: {hf.product_name}\n"
                    f"주의사항: {hint}"
                ).strip()
                emb = get_embedding(text)
                chunk_objs.append(
                    Chunk(
                        chunk_id=f"hf_{hf.id}_caution",
                        item_name=item_name,
                        section="hf_caution",
                        chunk_index=0,
                        text=text,
                        embedding=emb,
                    )
                )

            # function chunk
            if fn:
                text = (
                    f"제품명: {hf.product_name}\n"
                    f"기능성: {fn}"
                ).strip()
                emb = get_embedding(text)
                chunk_objs.append(
                    Chunk(
                        chunk_id=f"hf_{hf.id}_function",
                        item_name=item_name,
                        section="hf_function",
                        chunk_index=0,
                        text=text,
                        embedding=emb,
                    )
                )

            processed += 1
            if processed % 100 == 0:
                print(
                    f"  ... {processed}개 제품 처리 "
                    f"(현재까지 chunk {len(chunk_objs)}개 준비)"
                )

        # 3) 모아둔 Chunk 한 번에 INSERT
        if chunk_objs:
            Chunk.objects.bulk_create(chunk_objs, batch_size=1000)
        print(f"[LOAD] bulk_create로 Chunk {len(chunk_objs)}개 INSERT")

    print(
        f"[DONE] JSON {len(items)}개 중 "
        f"중복 제거 후 {processed}개 제품 처리 완료"
    )