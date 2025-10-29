plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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
        manifestPlaceholders["NAVER_MAP_CLIENT_ID"] = rootProject.extra.get("NAVER_MAP_CLIENT_ID") as? String ?: ""
        manifestPlaceholders["NAVER_MAP_CLIENT_SECRET"] = rootProject.extra.get("NAVER_MAP_CLIENT_SECRET") as? String ?: ""

        buildConfigField("String", "NAVER_CLIENT_ID", "\"${project.findProperty("NAVER_CLIENT_ID") ?: ""}\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"${project.findProperty("NAVER_CLIENT_SECRET") ?: ""}\"")

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
    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(21)
}


dependencies {
    implementation(project(":common"))
    implementation(project(":common:design"))
    implementation(project(":feature:main"))
    implementation(project(":feature:map"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:mypage"))
    implementation(project(":feature:news"))
    implementation(project(":feature:scheduler"))
    implementation(project(":feature:ChatBot"))
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    //app 모듈에서 네이버 지도 sdk 사용으로 의존성 추가
    implementation("io.github.fornewid:naver-map-compose:1.5.0")
    implementation("com.naver.maps:map-sdk:3.23.0")
    implementation("com.google.android.gms:play-services-location:20.0.0")
}