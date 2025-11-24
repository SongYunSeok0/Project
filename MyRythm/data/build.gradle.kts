import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

val localProps = Properties().apply {
    val f = rootProject.file("secret.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun prop(key: String) = localProps.getProperty(key) ?: ""

android {
    namespace = "com.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        buildConfigField("String", "BACKEND_BASE_URL", "\"http://10.0.2.2:8000/api/\"")
        buildConfigField("String", "NAVER_NEWS_BASE_URL", "\"https://openapi.naver.com/\"")
        buildConfigField("String", "NAVER_MAP_BASE_URL", "\"https://naveropenapi.apigw.ntruss.com/\"")

        // 3) 키 주입
        buildConfigField("String", "NAVER_MAP_CLIENT_ID", "\"${prop("NAVER_MAP_CLIENT_ID")}\"")
        buildConfigField("String", "NAVER_MAP_CLIENT_SECRET", "\"${prop("NAVER_MAP_CLIENT_SECRET")}\"")
        buildConfigField("String", "NAVER_NEWS_CLIENT_ID", "\"${prop("NAVER_CLIENT_ID")}\"")
        buildConfigField("String", "NAVER_NEWS_CLIENT_SECRET", "\"${prop("NAVER_CLIENT_SECRET")}\"")
    }

    // Compose 비활성화(데이터 계층은 UI 불필요)
    buildFeatures {
        compose = false
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
kotlin { jvmToolchain(21) }

dependencies {
    implementation(project(":domain"))
    implementation(project(":shared"))

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Retrofit + Moshi
    implementation(libs.retrofit)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.moshi.converter)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.coil.compose)
    implementation(libs.jsoup)
    implementation(libs.paging.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.security.crypto)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
}
