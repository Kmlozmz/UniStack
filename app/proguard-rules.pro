# =============================================================================
# UniStack ProGuard/R8 Rules
# =============================================================================

# -----------------------------------------------------------------------------
# General Android
# -----------------------------------------------------------------------------
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations, AnnotationDefault
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# Keep BuildConfig
-keep class com.unistack.app.BuildConfig { *; }

# -----------------------------------------------------------------------------
# Kotlin
# -----------------------------------------------------------------------------
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-dontwarn kotlinx.atomicfu.**
-dontwarn kotlinx.coroutines.debug.**

# -----------------------------------------------------------------------------
# Jetpack Compose
# -----------------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.animation.** { *; }
-dontwarn androidx.compose.**

# Compose compiler emits synthetic method for @Composable functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# -----------------------------------------------------------------------------
# AndroidX & Lifecycle
# -----------------------------------------------------------------------------
-keep class androidx.lifecycle.** { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Navigation Compose
-keep class androidx.navigation.** { *; }
-keepclassmembers class * {
    @androidx.navigation.NavArgument *;
}

# -----------------------------------------------------------------------------
# Room Database
# -----------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers @androidx.room.Entity class * { <fields>; <init>(...); }
-dontwarn androidx.room.paging.**

# UniStack Room entities
-keep class com.unistack.app.**.data.local.**Entity { *; }
-keep class com.unistack.app.**.data.local.**Dao { *; }
-keep class com.unistack.app.core.database.** { *; }

# -----------------------------------------------------------------------------
# DataStore
# -----------------------------------------------------------------------------
-keep class androidx.datastore.** { *; }
-keep class * extends androidx.datastore.core.Serializer { *; }

# -----------------------------------------------------------------------------
# Firebase
# -----------------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-keep interface com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firestore uses reflection for POJOs - keep all classes serialized to/from Firestore
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
}
-keepclasseswithmembers class * {
    @com.google.firebase.firestore.IgnoreExtraProperties *;
}

# UniStack domain models used with Firestore
-keep class com.unistack.app.feature_sync.data.** { *; }
-keep class com.unistack.app.feature_user.data.** { *; }
-keepclassmembers class com.unistack.app.**.domain.** {
    <init>(...);
    <fields>;
}

# -----------------------------------------------------------------------------
# Google Credentials (Sign-In)
# -----------------------------------------------------------------------------
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**

# -----------------------------------------------------------------------------
# Google Play Billing
# -----------------------------------------------------------------------------
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.api.**

# -----------------------------------------------------------------------------
# Coil (Image Loading)
# -----------------------------------------------------------------------------
-keep class coil.** { *; }
-dontwarn coil.**

# -----------------------------------------------------------------------------
# Coroutines
# -----------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**

# -----------------------------------------------------------------------------
# Serialization (if any Kotlinx serialization is used)
# -----------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# -----------------------------------------------------------------------------
# UniStack specific
# -----------------------------------------------------------------------------
# Keep MainActivity and Application
-keep class com.unistack.app.MainActivity { *; }
-keep class com.unistack.app.UniStackApplication { *; }

# Keep all Composable screens (accessed via reflection by navigation)
-keep @androidx.compose.runtime.Composable class * { *; }

# Keep sealed classes (many domain models are sealed)
-keepclassmembers class * extends kotlin.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# BroadcastReceivers
-keep class com.unistack.app.core.notifications.** { *; }

# -----------------------------------------------------------------------------
# Remove logging in release
# -----------------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# -----------------------------------------------------------------------------
# Prevent obfuscation of test-related classes
# -----------------------------------------------------------------------------
-dontwarn org.junit.**
-dontwarn junit.**
-dontwarn org.robolectric.**

# -----------------------------------------------------------------------------
# Keep line numbers for crash reports
# -----------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable

# -----------------------------------------------------------------------------
# Safe defaults - do not warn about missing classes we don't use
# -----------------------------------------------------------------------------
-dontwarn java.lang.invoke.**
-dontwarn org.jetbrains.annotations.**
-dontwarn javax.annotation.**
