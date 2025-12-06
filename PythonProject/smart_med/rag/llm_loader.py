# rag/llm_loader.py
import threading
from .llm import _load_model   # 반드시 _load_model이 tokenizer, model을 반환해야 함

_cached_tokenizer = None
_cached_model = None
_lock = threading.Lock()


def get_llm_model():
    global _cached_tokenizer, _cached_model

    # 이미 로드됨
    if _cached_tokenizer is not None and _cached_model is not None:
        return _cached_tokenizer, _cached_model

    # 스레드 안전 로드
    with _lock:
        if _cached_tokenizer is None or _cached_model is None:
            print("[LLM-LOADER] 모델 초기 로딩 시작")
            tokenizer, model = _load_model()   # ★★★ 여기서 반드시 튜플이 와야 함

            if tokenizer is None or model is None:
                raise RuntimeError("LLM 모델 로드 실패: _load_model()이 None을 반환함")

            _cached_tokenizer = tokenizer
            _cached_model = model

            print("[LLM-LOADER] 모델 로딩 완료")

    return _cached_tokenizer, _cached_model


def preload_qwen():
    print("[LLM-LOADER] preload_qwen() 호출됨 → 모델 미리 로드")
    return get_llm_model()
