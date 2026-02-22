plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.maven.publish)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        name = "KMP App Updater Compose UI"
        description = "Compose Multiplatform UI components for KMP App Updater — UpdateCard, UpdateBanner, DownloadProgressIndicator."
        inceptionYear = "2025"
        url = "https://github.com/pavi2410/kmp-app-updater/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "pavi2410"
                name = "Pavitra"
                url = "https://github.com/pavi2410/"
            }
        }
        scm {
            url = "https://github.com/pavi2410/kmp-app-updater/"
            connection = "scm:git:git://github.com/pavi2410/kmp-app-updater.git"
            developerConnection = "scm:git:ssh://git@github.com/pavi2410/kmp-app-updater.git"
        }
    }
}

kotlin {
    androidLibrary {
        namespace = "com.pavi2410.kmpappupdater.composeui"
        compileSdk = 36
        minSdk = 24
    }

    jvm("desktop")

    sourceSets {
        @Suppress("DEPRECATION")
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}
