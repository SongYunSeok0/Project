# rag/llm_loader.py
import threading
from .llm import _load_model   # 반드시 _load_model이 tokenizer, model을 반환해야 함

_cached_tokenizer = None
_cached_model = None
_lock = threading.Lock()


# rag/llm_loader.py
def get_llm_model():
    # LLM 사용 안 함 → None 반환
    return None, None


def preload_qwen():
    print("[LLM-LOADER] preload_qwen() 호출됨 → 모델 미리 로드")
    return get_llm_model()
