import time
import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

# 네가 쓰는 Qwen 병합 모델 경로
MODEL_PATH = r"C:\Users\user\Desktop\qwen_sft\merged_qwen25-3b-med"

_tokenizer = None
_model = None

# 프롬프트/생성 길이 제한 (속도에 직접 영향)
MAX_INSTRUCTION_CHARS = 3000    # instruction이 너무 길면 잘라서 토큰 수 줄이기
MAX_PROMPT_TOKENS = 768         # 입력 프롬프트 최대 토큰 수
MAX_NEW_TOKENS = 128             # 생성 길이 (기존 192 → 96으로 줄여서 속도 개선)

DEVICE = "cuda" if torch.cuda.is_available() else "cpu"

def _load_model():
    """
    내부용: 실제로 Qwen 모델을 로드하는 함수.
    이미 로드돼 있으면 그대로 토크나이저/모델을 반환한다.
    """
    global _tokenizer, _model

    # 이미 한 번 로드됐으면 다시 로드 안 함
    if _model is not None and _tokenizer is not None:
        return _tokenizer, _model

    print("Qwen 병합 모델 로드 중...")
    t0 = time.time()

    _tokenizer = AutoTokenizer.from_pretrained(
        MODEL_PATH,
        trust_remote_code=True,
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
        ).to(DEVICE)
    else:
        # CPU 환경이면 float32
        _model = AutoModelForCausalLM.from_pretrained(
            MODEL_PATH,
            dtype=torch.float32,
            trust_remote_code=True,
        )

    _model.eval()  # 추론 모드

    # 실제로 어디에 올라갔는지 확인용 로그
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
    """
    외부에서 쓰는 메인 함수.
    RAG에서 만들어준 instruction(프롬프트)을 받아서
    Qwen으로 답변을 생성한다.
    """
    tokenizer, model = _load_model()

    alpaca_prompt = _build_alpaca_prompt(instruction)

    # 토크나이즈 시 입력 길이 강하게 제한
    inputs = tokenizer(
        alpaca_prompt,
        return_tensors="pt",
        truncation=True,
        max_length=MAX_PROMPT_TOKENS,
    )

    # 입력 텐서를 모델이 올라간 device로 이동
    inputs = {k: v.to(model.device) for k, v in inputs.items()}

    input_len = inputs["input_ids"].shape[1]

    t0 = time.time()
    with torch.inference_mode():
        outputs = model.generate(
            **inputs,
            max_new_tokens=MAX_NEW_TOKENS,
            do_sample=False,              # greedy
            num_beams=1,                  # beam search 안 씀
            use_cache=True,
            eos_token_id=tokenizer.eos_token_id,
            pad_token_id=tokenizer.pad_token_id,
        )
    gen_elapsed = time.time() - t0

    # 입력 이후에 생성된 토큰만 사용
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
    return answer.strip()


def preload_qwen():
    """
    Django 서버 시작 시 미리 한 번 호출해서
    첫 질문에서 로딩 딜레이가 안 생기게 하는 용도.
    """
    _load_model()
