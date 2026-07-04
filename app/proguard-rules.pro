# --- SQLCipher (JNI) ---
-keep class net.zetetic.database.** { *; }
-keep,includedescriptorclasses class net.zetetic.database.sqlcipher.** { *; }

# --- kotlinx.serialization: keep @Serializable route classes for type-safe navigation ---
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class com.runtimelabs.clarity.navigation.** {
    *** Companion;
}
-keepclasseswithmembers class com.runtimelabs.clarity.navigation.** {
    kotlinx.serialization.KSerializer serializer(...);
}
