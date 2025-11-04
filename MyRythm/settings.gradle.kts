rootProject.name = "MyRythm"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.google.dagger.hilt.android") version "2.52" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
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
