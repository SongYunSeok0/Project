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

    // Compose BOM + 기본 세트
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.library)
    implementation(libs.bundles.core)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")

    // ★ 네이버 지도 + Compose
    implementation("com.naver.maps:map-sdk:3.23.0")
    implementation("io.github.fornewid:naver-map-compose:1.5.0")

    // ★ 현재 코드에서 FusedLocationProviderClient 직접 사용하므로 필요
    implementation("com.google.android.gms:play-services-location:20.0.0")

    // 기타
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //병원정보 하단시트
    implementation("androidx.compose.material3:material3:1.3.1")
}
