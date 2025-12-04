import time
import json

class APITimingMiddleware:
    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        start = time.time()
        response = self.get_response(request)
        duration = time.time() - start

        if "/api/rag/drug/" in request.path:
            print(f"[RAG TIME] {duration:.4f} sec | {request.method} {request.path}")

        return response

class RequestLoggingMiddleware:
    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        start = time.time()

        # 요청 정보 출력
        print("\n===== 📥 Incoming Request =====")
        print(f"PATH: {request.path}")
        print(f"METHOD: {request.method}")
        print(f"USER: {request.user if request.user.is_authenticated else 'Anonymous'}")

        # Body 출력 (JSON 요청만)
        try:
            body = request.body.decode("utf-8")
            if body:
                print(f"BODY: {body}")
        except:
            pass

        response = self.get_response(request)

        # 응답시간 계산
        duration = (time.time() - start) * 1000  # ms 단위

        # 응답 정보 출력
        print("===== 📤 Response =====")
        print(f"STATUS: {response.status_code}")
        print(f"DURATION: {duration:.2f}ms")
        print("=========================\n")

        return response

class DisableChunkedMiddleware:
    def __init__(self, get_response):
        self.get_response = get_response

    def __call__(self, request):
        response = self.get_response(request)

        # chunked encoding 방지
        if response.streaming:
            response.streaming = False
            response.content = b"".join(response.streaming_content)

        return response
