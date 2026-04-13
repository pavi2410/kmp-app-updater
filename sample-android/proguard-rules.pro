# Ktor — keep engine + serialization internals
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }
-dontwarn io.ktor.**
-dontwarn kotlinx.serialization.**

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**

# AndroidX WorkManager — Room generates WorkDatabase_Impl
-keep class androidx.work.impl.** { *; }

# AndroidX Startup
-keep class androidx.startup.** { *; }

# Keep kotlinx.datetime
-dontwarn kotlinx.datetime.**
