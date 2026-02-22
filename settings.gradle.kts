pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kmp-app-updater"

include(":core")
include(":compose-ui")
include(":sample:android")
include(":sample:desktop")
