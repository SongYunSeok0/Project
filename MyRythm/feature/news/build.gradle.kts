import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") // ✅ Compose 플러그인
}

android {
    namespace = "com.example.news"
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(project(":feature"))
    implementation(project(":common:design"))

    // ✅ Compose 필수 라이브러리
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui:1.7.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.3")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.foundation:foundation:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.compose.material:material-icons-extended:1.7.3")
    implementation("androidx.paging:paging-runtime-ktx:3.3.2")
    implementation("androidx.paging:paging-compose:3.3.2")
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // ✅ Retrofit & Coroutine
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("org.jsoup:jsoup:1.16.1")

    // ✅ 디버그용 UI 도구
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.3")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
