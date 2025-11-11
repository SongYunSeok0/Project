plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.map"

    compileSdk = rootProject.extra.get("compileSdk") as Int

    defaultConfig {
        minSdk = rootProject.extra.get("minSdk") as Int
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // 임시 하드코딩. 공백 제거
        buildConfigField("String", "NAVER_CLIENT_ID", "\"ff1FDMV_KytGQEHXntal\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"k3Jxk1Of5l\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions { jvmTarget = "21" }
    buildFeatures { compose = true; buildConfig = true }
}

dependencies {
    // 모듈
    implementation(project(":common:design"))
    implementation(project(":domain"))

    // Compose BOM + 기본 세트
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.library)
    implementation(libs.bundles.core)
    implementation(libs.bundles.test)

    // ★ 네이버 지도 + Compose
    implementation(libs.naver.map.sdk)
    implementation(libs.naver.map.compose)

    // ★ 현재 코드에서 FusedLocationProviderClient 직접 사용하므로 필요
    implementation(libs.play.services.location)

    // 기타
    implementation(libs.accompanist.permissions)

    implementation(libs.retrofit.converter.gson)
    implementation(libs.retrofit)

    implementation("androidx.compose.material:material-icons-extended")

}