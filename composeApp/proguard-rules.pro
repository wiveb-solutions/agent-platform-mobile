# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Compose
-keepclassmembers class ** {
    @com.jetbrains.compose.runtime.Composable <methods>;
}

# Keep Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }

# Keep Koin
-keep class io.insert-koin.** { *; }
-keepclassmembers class io.insert-koin.** { *; }

# Keep Voyager
-keep class cafe.adriel.voyager.** { *; }

# Keep Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# Keep models
-keep class com.wiveb.agentplatform.** { *; }

# Standard Android ProGuard rules
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-dontwarn sun.misc.**
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
