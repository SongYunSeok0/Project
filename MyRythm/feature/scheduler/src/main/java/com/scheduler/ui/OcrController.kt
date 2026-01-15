package com.scheduler.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.scheduler.ocr.OcrCropView


// 지도 컨트롤러 부분과 동일한 목적. 메모리누수&캡슐화 상태호이스팅 등...
// 네이버지도나 ML Kit 등 외부 라이브러리 명령형 API와 선언형 UI 중재자 역할

class OcrController {
    private var ocrView: OcrCropView? = null

    // View와 컨트롤러 연결
    fun setView(view: OcrCropView) {
        this.ocrView = view
    }

    // 이미지 경로 바인딩 위임
    fun bindImage(path: String) {
        ocrView?.bindImage(path)
    }

    // OCR 실행 및 복잡한 데이터(약품명, 횟수, 일수) 가공 전담
    fun analyze(
        onSuccess: (names: List<String>, times: Int?, days: Int?) -> Unit,
        onError: () -> Unit
    ) {
        val view = ocrView ?: return
        // OcrCropView의 결과를 받아 UI에 필요한 형태로 가공 (비즈니스 로직 캡슐화)
        view.setOnOcrParsed { list ->
            if (list.isEmpty()) {
                onError()
            } else {
                // Triple 리스트를 화면에 필요한 개별 값들로 변환
                // OcrScreen.kt 확인 버튼 부분의 코드를 컨트롤러로 이동
                val names = list.map { it.first }
                val maxTimes = list.mapNotNull { it.second }.maxOrNull()?.coerceIn(1, 6)
                val maxDays = list.mapNotNull { it.third }.maxOrNull()?.coerceAtLeast(1)

                onSuccess(names, maxTimes, maxDays)
            }
        }
        // 실제 OCR 작동 명령
        view.runOcr { /* 텍스트 로그 등이 필요할 경우 활용 */ }
    }

    // 메모리 누수 방지 등을 위한 리소스 정리용 코드
    fun cleanup() {
        ocrView = null
    }
}

// 컨트롤러 헬퍼 함수
@Composable
fun rememberOcrController(): OcrController {
    return remember { OcrController() }
}
