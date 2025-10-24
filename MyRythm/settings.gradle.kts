pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyRythm"
include(":app")
include(":feature")
include(":feature:login")
include(":feature:main")
include(":feature:mypage")
include(":feature:scheduler")
include(":feature:news")
include(":feature:map")
include(":data")
include(":domain")
include(":common")
include(":common:design")
