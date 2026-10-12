# ProGuard rules for OpenBooks

# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Keep Hilt/Dagger generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }

# Keep Room entities and DAOs
-keep class com.openbooks.domain.model.** { *; }
-keep class com.openbooks.data.db.** { *; }

# Keep Kotlin serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Coil
-keep class coil3.** { *; }
-dontwarn coil3.**

# Keep OkHttp/Retrofit
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep Moshi
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Keep EPUB library
-keep class nl.siegmann.epublib.** { *; }
-dontwarn nl.siegmann.epublib.**

# Keep PDFBox
-keep class org.apache.pdfbox.** { *; }
-dontwarn org.apache.pdfbox.**

# Keep Apache Commons
-keep class org.apache.commons.** { *; }
-dontwarn org.apache.commons.**

# Keep Juniper RAR
-keep class junrar.** { *; }
-dontwarn junrar.**

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep Material3
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Keep Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Hilt ViewModel
-keep class androidx.lifecycle.ViewModelProvider.** { *; }

# Keep generated Hilt modules
-keep class * extends dagger.hilt.android.HiltViewModel { *; }

# Keep data classes
-keepclassmembers class * {
    @kotlin.Metadata *;
}

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}