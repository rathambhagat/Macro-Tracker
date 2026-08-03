# Room entities
-keep class com.bunkmarte.data.model.** { *; }

# Kotlin enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Compose
-dontwarn androidx.compose.**
