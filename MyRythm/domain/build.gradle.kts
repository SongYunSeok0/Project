plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
kotlin {
    jvmToolchain(21) // compilerOptions 대신 이 한 줄이면 충분
}
dependencies{
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
}