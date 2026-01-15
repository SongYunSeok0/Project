// app/build.gradle.kts
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.myrhythm"
    compileSdk = 36

    val secretProps = Properties().apply {
        val f = rootProject.file("secret.properties")
        if (f.exists()) load(f.inputStream())
    }

    signingConfigs {
        getByName("debug") {
            // secret.properties의 개인 구글 debug.keystore.path 주소
            // 기본 주소값이 맞지 않을 경우 개인 secret.properties에 debug.keystore.path=%본인/debug.keystore주소 기재할 것
            val customKeystorePath = secretProps.getProperty("debug.keystore.path")

            if (customKeystorePath != null) {
                storeFile = file(customKeystorePath)
            } else {
                // secret.properties에 구글 키 주소가 없을 경우 기존의 키 주소 기본값 사용하기
                storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")            }

            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "com.myrhythm"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val props = Properties().apply {
            val f = rootProject.file("secret.properties")
            if (f.exists()) load(f.inputStream())
        }
        manifestPlaceholders["NAVER_MAP_CLIENT_ID"] =
            props.getProperty("NAVER_MAP_CLIENT_ID", "")

        val kakaoAppKey = props.getProperty("KAKAO_NATIVE_APP_KEY", "")
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoAppKey
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoAppKey\"")
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
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
    implementation(project(":shared"))
    implementation(project(":feature:map"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:mypage"))
    implementation(project(":feature:news"))
    implementation(project(":feature:healthinsight"))
    implementation(project(":feature:scheduler"))
    implementation(project(":feature:chatbot"))

    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.library)
    implementation(libs.bundles.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.test)

    implementation(libs.naver.map.sdk)

    implementation(libs.hilt.android)
    implementation(libs.androidx.monitor)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.retrofit)

    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.converter)

    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.bundles.test)

    implementation("com.kakao.sdk:v2-user:2.11.0")

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)

    implementation(libs.androidx.health.connect)
    implementation(libs.accompanist.swiperefresh)
}