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
