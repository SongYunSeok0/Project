import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.news"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // ✅ local.properties 에서 API 키 안전하게 읽기
        val localProps = rootProject.file("local.properties")
        val props = Properties()

        if (localProps.exists()) {
            props.load(localProps.inputStream())
            val naverClientId: String = props.getProperty("NAVER_CLIENT_ID", "")
            val naverClientSecret: String = props.getProperty("NAVER_CLIENT_SECRET", "")

            buildConfigField("String", "NAVER_CLIENT_ID", "\"$naverClientId\"")
            buildConfigField("String", "NAVER_CLIENT_SECRET", "\"$naverClientSecret\"")
        } else {
            buildConfigField("String", "NAVER_CLIENT_ID", "\"\"")
            buildConfigField("String", "NAVER_CLIENT_SECRET", "\"\"")
        }
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
        // ✅ Compose 및 BuildConfig 둘 다 활성화
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":common:design"))
    implementation(project(":domain"))

    // ✅ Compose 필수 라이브러리
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.compose.library)
    implementation(libs.bundles.core)
    implementation(libs.bundles.test)

    // ✅ Retrofit & Coroutine
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("androidx.compose.material:material-icons-extended:1.5.0")
    implementation("androidx.paging:paging-compose:3.3.2")

    // ✅ 디버그용 UI 도구
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.3")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
