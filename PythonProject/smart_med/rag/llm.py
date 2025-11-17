# rag/llm.py
import time
import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

MODEL_PATH = r"C:\Users\user\Desktop\qwen_sft\merged_qwen25-3b-med"

_tokenizer = None
_model = None

# 프롬프트/생성 길이 제한 (속도에 직접 영향)
MAX_INSTRUCTION_CHARS = 3000   # instruction이 너무 길면 잘라서 토큰 수 줄이기
MAX_PROMPT_TOKENS = 512        # 입력 토큰 상한
MAX_NEW_TOKENS = 96            # 생성 토큰 수 (128 → 96로 더 줄임)


def _load_model():
    global _tokenizer, _model
    if _model is None:
        print("Qwen 병합 모델 로드 중...")
        t0 = time.time()

        _tokenizer = AutoTokenizer.from_pretrained(
            MODEL_PATH,
            trust_remote_code=True,
        )
        _model = AutoModelForCausalLM.from_pretrained(
            MODEL_PATH,
            device_map="cuda",              # GPU 사용
            torch_dtype=torch.float16,
            trust_remote_code=True,
        )
        _model.eval()  # 추론 모드

        # 실제로 어디에 올라갔는지 확인용 로그
        try:
            any_param = next(_model.parameters())
            print(f"Qwen device: {any_param.device}")
        except StopIteration:
            print("Qwen device: <no parameters?>")

        print(f"Qwen 로드 완료, elapsed={time.time() - t0:.2f}s")

    return _tokenizer, _model


def _build_alpaca_prompt(instruction: str) -> str:
    # instruction 안에 이미 [참고 문서], [질문], [지시] 등이 들어감
    # 너무 길면 앞쪽만 사용해서 토큰 수 제한
    if len(instruction) > MAX_INSTRUCTION_CHARS:
        instruction = instruction[:MAX_INSTRUCTION_CHARS]

    return f"""Below is an instruction that describes a task. Write a response that appropriately completes the request.

### Instruction:
{instruction}

### Response:
"""


def generate_answer(instruction: str) -> str:
    tokenizer, model = _load_model()

    alpaca_prompt = _build_alpaca_prompt(instruction)

    # 토크나이즈 시 입력 길이 강하게 제한
    inputs = tokenizer(
        alpaca_prompt,
        return_tensors="pt",
        truncation=True,
        max_length=MAX_PROMPT_TOKENS,   # 1024 → 512로 줄임
    ).to(model.device)

    # 실제 생성 시간 측정해 보기
    t0 = time.time()
    with torch.inference_mode():
        outputs = model.generate(
            **inputs,
            max_new_tokens=MAX_NEW_TOKENS,        # 128 → 96
            do_sample=False,                      # greedy, 조금 더 빠름
            num_beams=1,                          # beam search 안 씀
            use_cache=True,
            eos_token_id=tokenizer.eos_token_id,
            pad_token_id=tokenizer.eos_token_id,
        )
    gen_elapsed = time.time() - t0
    print(f"[LLM] generate elapsed={gen_elapsed:.2f}s")

    # ⬇ 입력 이후에 생성된 토큰만 사용
    output_ids = outputs[0]
    input_len = inputs["input_ids"].shape[1]
    generated_ids = output_ids[input_len:]

    if len(generated_ids) == 0:
        return ""

    answer = tokenizer.decode(generated_ids, skip_special_tokens=True)
    return answer.strip()
