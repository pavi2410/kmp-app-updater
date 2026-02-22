plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":compose-ui"))
                implementation(compose.desktop.currentOs)
                @Suppress("DEPRECATION")
                implementation(compose.material3)
                implementation(libs.ktor.client.java)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.pavi2410.appupdater.sample.MainKt"

        nativeDistributions {
            packageName = "kmp-app-updater-sample"
            packageVersion = "1.0.0"
            description = "KMP App Updater sample desktop app"
            vendor = "pavi2410"

            linux {
                debPackageVersion = "1.0.0"
            }
            windows {
                msiPackageVersion = "1.0.0"
            }
            macOS {
                dmgPackageVersion = "1.0.0"
            }
        }
    }
}
