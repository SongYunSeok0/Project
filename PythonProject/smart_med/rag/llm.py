import time
import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

MODEL_PATH = "/models/merged_qwen25-3b-med"

_tokenizer = None
_model = None

# ⭐ 최적화된 설정
MAX_INSTRUCTION_CHARS = 2000  # 3000 → 2000 (입력 짧게)
MAX_PROMPT_TOKENS = 512       # 768 → 512 (입력 토큰 감소)
MAX_NEW_TOKENS = 320          # 256 → 320 (적절한 중간값)

DEVICE = "cuda" if torch.cuda.is_available() else "cpu"

def _load_model():
    global _tokenizer, _model

    # 이미 한 번 로드됐으면 다시 로드 안 함
    if _model is not None and _tokenizer is not None:
        return _tokenizer, _model

    print("Qwen 병합 모델 로드 중...")
    t0 = time.time()

    _tokenizer = AutoTokenizer.from_pretrained(
        MODEL_PATH,
        trust_remote_code=True,
        local_files_only=True,   # ← 추가
    )

    # pad_token 없으면 eos로 맞춰 두기 (generate 시 경고 방지)
    if _tokenizer.pad_token is None:
        _tokenizer.pad_token = _tokenizer.eos_token

    if DEVICE == "cuda":
        # GPU + float16
        _model = AutoModelForCausalLM.from_pretrained(
            MODEL_PATH,
            dtype=torch.float16,
            trust_remote_code=True,
            local_files_only=True,  # ← 추가
        ).to(DEVICE)
    else:
        # CPU 환경이면 float32
        _model = AutoModelForCausalLM.from_pretrained(
            MODEL_PATH,
            dtype=torch.float32,
            trust_remote_code=True,
            local_files_only=True,  # ← 추가
        )

    _model.eval()

    # 실제로 어디에 올라갔는지 확인용 로그
    try:
        any_param = next(_model.parameters())
        print(f"Qwen device: {any_param.device}, dtype: {any_param.dtype}")
    except StopIteration:
        print("Qwen device: <no parameters?>")

    print(f"Qwen 로드 완료, elapsed={time.time() - t0:.2f}s")
    return _tokenizer, _model


def _build_alpaca_prompt(instruction: str) -> str:
    """Alpaca 스타일 프롬프트"""
    if len(instruction) > MAX_INSTRUCTION_CHARS:
        instruction = instruction[:MAX_INSTRUCTION_CHARS]

    return f"""Below is an instruction that describes a task. Write a response that appropriately completes the request.

### Instruction:
{instruction}

### Response:
"""

def generate_answer(instruction: str) -> str:
    """LLM으로 답변 생성"""
    tokenizer, model = _load_model()

    alpaca_prompt = _build_alpaca_prompt(instruction)

    inputs = tokenizer(
        alpaca_prompt,
        return_tensors="pt",
        truncation=True,
        max_length=MAX_PROMPT_TOKENS,
    )

    inputs = {k: v.to(model.device) for k, v in inputs.items()}
    input_len = inputs["input_ids"].shape[1]

    t0 = time.time()
    with torch.inference_mode():
        outputs = model.generate(
            **inputs,
            max_new_tokens=MAX_NEW_TOKENS,
            do_sample=False,
            num_beams=1,
            use_cache=True,
            eos_token_id=tokenizer.eos_token_id,
            pad_token_id=tokenizer.pad_token_id,
            # ⭐ 문장 끝에서 자연스럽게 종료
            early_stopping=True,
        )
    gen_elapsed = time.time() - t0

    output_ids = outputs[0]
    generated_ids = output_ids[input_len:]

    print(
        f"[LLM] input_tokens={input_len}, "
        f"output_tokens={len(generated_ids)}, "
        f"elapsed={gen_elapsed:.2f}s"
    )

    if len(generated_ids) == 0:
        return ""

    answer = tokenizer.decode(generated_ids, skip_special_tokens=True)

    # ⭐ 문장 단위로 깔끔하게 끊기
    answer = _truncate_to_sentence(answer.strip())

    return answer


def _truncate_to_sentence(text: str) -> str:
    """
    답변이 중간에 끊겼다면 마지막 완전한 문장까지만 사용
    """
    if not text:
        return text

    # 한국어 문장 종결 기호 (우선순위 순)
    sentence_enders = [
        "습니다.", "ㅂ니다.", "니다.",
        "세요.", "요.", "다.",
        "다!", "요!", "다?", "요?",
    ]

    # 마지막 문장 종결 위치 찾기
    last_end = -1
    best_ender = ""

    for ender in sentence_enders:
        pos = text.rfind(ender)
        if pos > last_end:
            last_end = pos + len(ender)
            best_ender = ender

    # 문장 종결을 찾았고, 최소 50% 이상 생성되었으면 거기까지만 반환
    if last_end > 0 and last_end > len(text) * 0.5:
        result = text[:last_end].strip()
        print(f"[TRUNCATE] 문장 종결 '{best_ender}'에서 자름: {len(text)} → {len(result)} chars")
        return result

    # 문장 종결이 없거나 너무 앞쪽이면
    # 마지막 단락 제거 (불완전한 단락일 가능성)
    if '\n' in text:
        paragraphs = text.split('\n')
        if len(paragraphs) > 1 and len(paragraphs[-1]) < 30:
            result = '\n'.join(paragraphs[:-1]).strip()
            print(f"[TRUNCATE] 마지막 단락 제거: {len(text)} → {len(result)} chars")
            return result

    # 최후: 원본 그대로
    return text


def preload_qwen():
    """서버 시작 시 미리 모델 로드"""
    _load_model()
