plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.maven.publish)
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    pom {
        name = "KMP App Updater Core"
        description = "Headless KMP library for in-app updates — models, update sources, downloader, installer."
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
        namespace = "com.pavi2410.kmpappupdater.core"
        compileSdk = 36
        minSdk = 24

        withHostTest {}
    }

    jvm("desktop")

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.work.runtime.ktx)
        }


        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.java)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.client.mock)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}
