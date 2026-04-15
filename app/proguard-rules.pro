# Add project specific ProGuard rules here.
# You can control the default ProGuard files for this project using the
# buildTypes block in your module-level build.gradle.kts file.

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-dontwarn kotlinx.coroutines.internal.MainDispatcherFactory$-CC
