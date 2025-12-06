import time
import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

#MODEL_PATH = r"C:\Users\user\Desktop\qwen_sft\merged_qwen25-3b-med"
MODEL_PATH = "/models/merged_qwen25-3b-med"

_tokenizer = None
_model = None

# 프롬프트/생성 길이 제한 (속도에 직접 영향)
MAX_INSTRUCTION_CHARS = 3000    # instruction이 너무 길면 잘라서 토큰 수 줄이기
MAX_PROMPT_TOKENS = 768         # 입력 프롬프트 최대 토큰 수
MAX_NEW_TOKENS = 256            # 생성 길이 (기존 192 → 96으로 줄여서 속도 개선)

DEVICE = "cuda" if torch.cuda.is_available() else "cpu"

def _load_model():
    global _tokenizer, _model

    if _model is not None and _tokenizer is not None:
        return _tokenizer, _model

    print("Qwen 병합 모델 로드 중...")
    t0 = time.time()

    _tokenizer = AutoTokenizer.from_pretrained(
        MODEL_PATH,
        trust_remote_code=True,
        local_files_only=True,   # ← 추가
    )

    if _tokenizer.pad_token is None:
        _tokenizer.pad_token = _tokenizer.eos_token

    if DEVICE == "cuda":
        _model = AutoModelForCausalLM.from_pretrained(
            MODEL_PATH,
            dtype=torch.float16,
            trust_remote_code=True,
            local_files_only=True,  # ← 추가
        ).to(DEVICE)
    else:
        _model = AutoModelForCausalLM.from_pretrained(
            MODEL_PATH,
            dtype=torch.float32,
            trust_remote_code=True,
            local_files_only=True,  # ← 추가
        )

    _model.eval()

    try:
        any_param = next(_model.parameters())
        print(f"Qwen device: {any_param.device}, dtype: {any_param.dtype}")
    except StopIteration:
        print("Qwen device: <no parameters?>")

    print(f"Qwen 로드 완료, elapsed={time.time() - t0:.2f}s")
    return _tokenizer, _model



def _build_alpaca_prompt(instruction: str) -> str:
    """
    Alpaca 스타일 프롬프트 래핑.
    instruction 안에 [참고 문서], [질문], [지시]까지 모두 포함되어 들어온다고 가정.
    """
    # instruction이 너무 길면 앞부분만 사용해서 토큰 수 제한
    if len(instruction) > MAX_INSTRUCTION_CHARS:
        instruction = instruction[:MAX_INSTRUCTION_CHARS]

    return f"""Below is an instruction that describes a task. Write a response that appropriately completes the request.

### Instruction:
{instruction}

### Response:
"""

def generate_answer(instruction: str) -> str:
    try:
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
            )

        gen_elapsed = time.time() - t0
        print(f"[LLM] generate elapsed={gen_elapsed:.2f}s")

        generated_ids = outputs[0][input_len:]
        return tokenizer.decode(generated_ids, skip_special_tokens=True).strip()

    except Exception as e:
        print("🔥🔥🔥 LLM ERROR OCCURRED 🔥🔥🔥")
        print("Error:", e)
        import traceback
        traceback.print_exc()

        return "현재 AI 응답 생성 중 오류가 발생했습니다."




def preload_qwen():
    """
    Django 서버 시작 시 미리 한 번 호출해서
    첫 질문에서 로딩 딜레이가 안 생기게 하는 용도.
    """
    _load_model()
