// app/build.gradle.kts
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.myrhythm"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.myrhythm"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //manifestPlaceholders["NAVER_MAP_CLIENT_ID"] = rootProject.extra.get("NAVER_MAP_CLIENT_ID") as? String ?: ""
        //manifestPlaceholders["NAVER_MAP_CLIENT_SECRET"] = rootProject.extra.get("NAVER_MAP_CLIENT_SECRET") as? String ?: ""

        // 네이버 지도 SDK용 클라이언트 ID만 주입
        val props = Properties().apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) load(f.inputStream())
        }
        manifestPlaceholders["NAVER_MAP_CLIENT_ID"] =
            props.getProperty("NAVER_MAP_CLIENT_ID", "")

        //카카오
        val kakaoAppKey = props.getProperty("KAKAO_NATIVE_APP_KEY", "")
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoAppKey
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoAppKey\"")
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

}

kotlin { jvmToolchain(21) }

dependencies {
    // 모듈 연결
    implementation(project(":common"))
    implementation(project(":common:design"))
    implementation(project(":feature:main"))
    implementation(project(":feature:map"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:mypage"))
    implementation(project(":feature:news"))
    implementation(project(":feature:scheduler"))
    implementation(project(":feature:chatbot"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":core"))

    // Compose / Core
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.library)
    implementation(libs.bundles.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // 테스트
    testImplementation(libs.bundles.test)

    //카카오
    implementation("com.kakao.sdk:v2-user:2.11.0")
}