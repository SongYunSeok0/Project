import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.auth"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")


        val props = Properties().apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) load(f.inputStream())
        }
        val googleClientId = props.getProperty("GOOGLE_CLIENT_ID", "")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
    }

    buildFeatures {
        buildConfig = true
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
    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(project(":common:design"))
    implementation(project(":feature:main"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":core"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.compose.library)

    implementation(libs.bundles.core)
    implementation(libs.bundles.test)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    //카카오
    implementation("com.kakao.sdk:v2-user:2.11.0")
    // 스트럭처로 구글 로그인 연동용 라이브러리 의존성 추가(최신버전이용)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    // 구글 로그인 하위버전 호환용 라이브러리 추가
    implementation("androidx.credentials:credentials:1.2.0")
}