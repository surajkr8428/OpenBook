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
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "OpenBooks"
include(":app")
include(":core:domain")
include(":core:data")
include(":core:parser-epub")
include(":core:parser-pdf")
include(":core:parser-comics")
include(":core:parser-lcp")
include(":core:scanner")
include(":core:opds")
include(":core:tts")
include(":core:sync")
include(":feature:library")
include(":feature:reader")
include(":feature:settings")
include(":feature:store")
include(":feature:onboarding")
include(":feature:permissions")
include(":feature:annotations")