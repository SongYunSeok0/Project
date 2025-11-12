rootProject.name = "MyRythm"

pluginManagement {
    plugins {
        // ✅ Firebase Google Services 플러그인 추가
        id("com.google.gms.google-services") version "4.4.4" apply false
    }
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://repository.map.naver.com/archive/maven") }
    }
}

include(
    ":app",
    ":common",
    ":common:design",
    ":feature:auth",
    ":feature:main",
    ":feature:map",
    ":feature:mypage",
    ":feature:news",
    ":feature:scheduler",
    ":feature:chatbot"
)
include(":data")
include(":domain")
include(":core")
