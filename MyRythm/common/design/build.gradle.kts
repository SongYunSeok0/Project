plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {

    namespace = "com.common.design"

    compileSdk = 36

    defaultConfig { minSdk = 26 }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.compose.library)
    implementation(libs.bundles.core)
    implementation(libs.bundles.test)

}
