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

    defaultConfig {
        applicationId = "com.myrhythm"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- local.properties에서 안전하게 로드 ---
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
    implementation(project(":core"))

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
    implementation(libs.retrofit.converter.gson)

    testImplementation(libs.bundles.test)

    //카카오
    implementation("com.kakao.sdk:v2-user:2.11.0")
    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)


}
