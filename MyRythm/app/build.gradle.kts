// app/build.gradle.kts
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.myrhythm"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.myrythm"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- local.properties에서 안전하게 로드 ---
        val props = Properties().apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) load(f.inputStream())
        }
        val mapId      = props.getProperty("NAVER_MAP_CLIENT_ID", "")
        val mapSecret  = props.getProperty("NAVER_MAP_CLIENT_SECRET", "")
        val openId     = props.getProperty("NAVER_CLIENT_ID", "")
        val openSecret = props.getProperty("NAVER_CLIENT_SECRET", "")

        val kakaoAppKey = props.getProperty("KAKAO_NATIVE_APP_KEY", "")

        // AndroidManifest placeholders (네이버 지도 SDK용)
        manifestPlaceholders["NAVER_MAP_CLIENT_ID"] = mapId
        manifestPlaceholders["NAVER_MAP_CLIENT_SECRET"] = mapSecret
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoAppKey

        // BuildConfig (오픈 API 호출용)
        buildConfigField("String", "NAVER_CLIENT_ID", "\"$openId\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"$openSecret\"")
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
        isCoreLibraryDesugaringEnabled = true
    }
    //kotlinOptions { jvmTarget = "21" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin { jvmToolchain(21) }

dependencies {
    implementation(project(":common"))
    implementation(project(":common:design"))
    implementation(project(":feature:main"))
    implementation(project(":feature:map"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:mypage"))
    implementation(project(":feature:news"))
    implementation(project(":feature:scheduler"))
    implementation(project(":feature:chatbot"))

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.library)
    implementation(libs.bundles.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.bundles.test)

    implementation("com.naver.maps:map-sdk:3.23.0")

    implementation("com.kakao.sdk:v2-user:2.11.0")
}
