// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

// --- 이 블록을 파일 맨 아래에 추가하세요 ---
// 프로젝트의 모든 모듈에서 공통으로 사용할 버전 변수를 정의합니다.
ext {
    set("compileSdk", 36)
    set("minSdk", 24)
    set("targetSdk", 36)
}
