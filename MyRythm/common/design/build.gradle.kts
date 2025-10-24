plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.common.design"
    compileSdk = 34

    defaultConfig { minSdk = 24 }

    buildFeatures { compose = true }

}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
}
