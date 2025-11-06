// app/build.gradle.kts
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.myrythm"
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

        // AndroidManifest placeholders (네이버 지도 SDK용)
        manifestPlaceholders["NAVER_MAP_CLIENT_ID"] = mapId
        manifestPlaceholders["NAVER_MAP_CLIENT_SECRET"] = mapSecret

        // BuildConfig (오픈 API 호출용)
        buildConfigField("String", "NAVER_CLIENT_ID", "\"$openId\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"$openSecret\"")
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
    kotlinOptions { jvmTarget = "21" }
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

    // domain & data modules
    implementation(project(":domain"))
    implementation(project(":data"))

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.library)
    implementation(libs.bundles.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.bundles.test)

    implementation("com.naver.maps:map-sdk:3.23.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.4.0")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")

    // EXIF (사진 회전 보정)
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // ML Kit (한글 OCR)
    implementation("com.google.mlkit:text-recognition-korean:16.0.1")
}
