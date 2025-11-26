import time

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
