// app/build.gradle.kts
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)

    // ✅ Firebase 플러그인 추가
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.myrythm"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.myrythm"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 네이버 지도 SDK용 클라이언트 ID만 주입
        val props = Properties().apply {
            val f = rootProject.file("secret.properties")
            if (f.exists()) load(f.inputStream())
        }
        manifestPlaceholders["NAVER_MAP_CLIENT_ID"] =
            props.getProperty("NAVER_MAP_CLIENT_ID", "")
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

    // ✅ Firebase Messaging 의존성 추가
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)


    // 테스트
    testImplementation(libs.bundles.test)
}
