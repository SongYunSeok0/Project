rootProject.name = "MyRythm"

pluginManagement {
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
        maven { url = java.net.URI("https://devrepo.kakao.com/nexus/content/groups/public/") }
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
