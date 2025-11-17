from typing import List
from sentence_transformers import SentenceTransformer

MODEL_NAME = "sentence-transformers/all-MiniLM-L6-v2"
EMD_DIM = 384

_model = None

def _get_model():
    global _model
    if _model is None:
        _model = SentenceTransformer(MODEL_NAME)
    return _model

def get_embedding(text: str) -> list[float]:
    text = " ".join(text.split())
    model = _get_model()
    emb = model.encode(text, normalize_embeddings=True)
    return emb.tolist()