import java.io.File
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = File(settings.rootDir, "local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

// pluginManagement 블록: Gradle 플러그인을 어디서 찾을지 설정
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("org\\.jetbrains\\.kotlin.*")
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
        // 네이버 지도 SDK를 위한 저장소 주소
        maven {
            url = uri("https://repository.map.naver.com/archive/maven")
        }
    }
}

rootProject.name = "MyRythm"

include(
    ":app",
    ":common",
    ":common:design",
    ":data",
    ":domain",
    ":feature:login",
    ":feature:main",
    ":feature:map",
    ":feature:mypage",
    ":feature:news",
    ":feature:scheduler",
    ":feature:ChatBot"
)

// 5. --- allprojects 대신 사용할 올바른 코드 ---
// 각 하위 프로젝트(모듈)가 평가되기 전에 공통 로직을 실행
gradle.beforeProject{
    // local.properties의 각 키-값 쌍을 모든 모듈의 'extra' 속성으로 추가
    localProperties.forEach { key, value ->
        extra.set(key.toString(), value)
    }
}
