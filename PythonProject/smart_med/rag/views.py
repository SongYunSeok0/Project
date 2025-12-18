# rag/views.py
import time
import traceback

from rest_framework.views import APIView
from rest_framework.response import Response

from .services import retrieve_top_chunks, build_answer
from .tasks import run_rag_task
from .utils import (
    is_medical_question,
    get_non_medical_response,
    is_greeting,  # 추가
    get_greeting_response,  # 추가
)
from celery.result import AsyncResult


class DrugRAGView(APIView):
    """
    약학 RAG 질의 처리 View
    - mode=async  : Celery 비동기 처리
    - mode=sync   : 기존 방식(즉시 응답)
    """
    authentication_classes = []
    permission_classes = []

    def post(self, request):
        question = request.data.get("question")
        mode = request.data.get("mode", "async")

        # 디버깅용 로그
        print(f"[DEBUG] 요청 본문: {request.data}")
        print(f"[DEBUG] question='{question}', mode='{mode}'")

        if not question:
            return Response({"detail": "question 필드가 필요합니다."}, status=400)

        # ========== 최우선 1순위: 인사말 처리 (LLM/DB 조회 없이 즉시 응답) ==========
        if is_greeting(question):
            print(f"[GREETING-VIEW] 인사말 즉시 응답: '{question}'")
            return Response(
                {
                    "status": "done",
                    "question": question,
                    "result": {
                        "answer": get_greeting_response(),
                        "contexts": [],
                    },
                },
                status=200
            )
        # =================================================================

        # ========== 2순위: 의료 질문 검증 ==========
        if not is_medical_question(question):
            print(f"[API-FILTER] 비의료 질문 차단: '{question}'")
            return Response(
                {
                    "status": "rejected",
                    "question": question,
                    "result": {
                        "answer": get_non_medical_response(),
                        "contexts": [],
                    },
                },
                status=200
            )
        # ==========================================

        # ========== 3순위: 비동기 모드 (Celery) ==========
        if mode == "async":
            task = run_rag_task.delay(question)
            return Response(
                {"task_id": task.id, "status": "processing"},
                status=202,
            )

        # ========== 4순위: 동기 모드 (즉시 RAG 처리) ==========
        try:
            start = time.time()
            chunks = retrieve_top_chunks(question, k=5)
            answer = build_answer(question, chunks)

            elapsed = time.time() - start
            print(f"[SYNC-RAG] q='{question[:30]}' elapsed={elapsed:.2f}s")

        except Exception as e:
            print(f"[ERROR] RAG 처리 오류: {e}")
            traceback.print_exc()
            return Response(
                {"detail": "RAG 처리 중 오류", "error": str(e)},
                status=500
            )

        # ----------------------
        # fallback context 적용
        # ----------------------
        if not chunks:
            contexts = [{
                "chunk_id": "",
                "item_name": "",
                "section": "",
                "chunk_index": 0
            }]
        else:
            contexts = [
                {
                    "chunk_id": c.chunk_id,
                    "item_name": c.item_name,
                    "section": c.section,
                    "chunk_index": c.chunk_index,
                }
                for c in chunks
            ]

        return Response(
            {
                "status": "done",
                "question": question,
                "result": {
                    "answer": answer,
                    "contexts": contexts,
                },
            },
            status=200
        )


class RAGTaskResultView(APIView):
    authentication_classes = []
    permission_classes = []

    def get(self, request, task_id):
        result = AsyncResult(task_id)

        if result.state in ["PENDING", "RECEIVED"]:
            return Response({
                "status": "pending",
                "question": None,
                "result": None
            })

        if result.state == "STARTED":
            return Response({
                "status": "processing",
                "question": None,
                "result": None
            })

        if result.state == "FAILURE":
            return Response({
                "status": "failed",
                "question": None,
                "result": None,
                "error": str(result.result)
            }, status=500)

        if result.state == "SUCCESS":
            data = result.result  # Celery task 반환값
            
            # 🔥 task 내부에서 실패한 경우 처리
            if data.get("status") == "failed":
                return Response({
                    "status": "failed",
                    "question": data.get("question"),
                    "result": None,
                    "error": data.get("error", "Unknown error")
                }, status=500)
            
            # 🔥 성공한 경우
            return Response(
                {
                    "status": "done",
                    "question": data.get("question", ""),
                    "result": {
                        "answer": data.get("result", {}).get("answer", ""),
                        "contexts": data.get("result", {}).get("contexts", []),
                    },
                },
                status=200
            )

        # 예외적인 상태
        return Response({
            "status": result.state,
            "question": None,
            "result": None
        }, status=200)
