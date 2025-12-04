from rest_framework import viewsets, permissions
from rest_framework.response import Response
from rest_framework.decorators import action
from drf_spectacular.utils import extend_schema, OpenApiResponse, OpenApiExample

from .models import HeartRate, DailyStep
from .serializers import HeartRateSerializer, DailyStepSerializer


@extend_schema(
    tags=["Health - Heart Rate"],
    summary="심박수 기록 목록 조회",
    description="로그인한 사용자의 전체 심박수 기록을 최신순으로 조회합니다.",
    responses={200: HeartRateSerializer(many=True)},
)
class HeartRateViewSet(viewsets.ModelViewSet):
    queryset = HeartRate.objects.all()
    serializer_class = HeartRateSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return HeartRate.objects.filter(user=self.request.user).order_by(
            "-collected_at", "-id"
        )

    @extend_schema(
        summary="최신 심박수 조회",
        description="가장 최신의 심박수(bpm, collected_at)를 반환합니다. 데이터가 없으면 null 반환.",
        responses={
            200: OpenApiResponse(
                examples=[
                    OpenApiExample(
                        "데이터 있음",
                        value={"bpm": 72, "collected_at": "2025-12-03T10:30:00Z"},
                    ),
                    OpenApiExample(
                        "데이터 없음",
                        value={"bpm": None, "collected_at": None},
                    ),
                ]
            )
        },
        tags=["Health - Heart Rate"],
    )
    @action(detail=False, methods=["get"])
    def latest(self, request):
        qs = self.get_queryset().order_by("-collected_at", "-id")
        obj = qs.first()

        if not obj:
            return Response({"bpm": None, "collected_at": None})

        return Response(
            {
                "bpm": obj.bpm,
                "collected_at": obj.collected_at,
            }
        )

    @extend_schema(
        summary="심박수 기록 생성",
        description="새로운 심박수 데이터(bpm, collected_at)를 생성합니다.",
        request=HeartRateSerializer,
        responses={201: HeartRateSerializer},
        tags=["Health - Heart Rate"],
    )
    def create(self, request, *args, **kwargs):
        return super().create(request, *args, **kwargs)



@extend_schema(
    tags=["Health - Daily Step"],
    summary="하루 걸음수 조회",
    description="로그인한 사용자의 일별 걸음수 기록 전체를 조회합니다.",
    responses={200: DailyStepSerializer(many=True)},
)
class DailyStepViewSet(viewsets.ModelViewSet):
    queryset = DailyStep.objects.all()
    serializer_class = DailyStepSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_queryset(self):
        return DailyStep.objects.filter(user=self.request.user)

    @extend_schema(
        summary="하루 걸음수 생성 또는 업데이트",
        description="""
같은 날짜(date)의 데이터가 이미 존재하면 steps 값을 업데이트하고,
없으면 새로운 기록을 생성합니다.
        """,
        request=DailyStepSerializer,
        responses={
            200: DailyStepSerializer,
            201: DailyStepSerializer,
        },
        tags=["Health - Daily Step"],
    )
    def create(self, request, *args, **kwargs):
        return super().create(request, *args, **kwargs)