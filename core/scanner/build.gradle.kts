plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.android.library")
}

android {
    namespace = "com.openbooks.scanner"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
        targetSdk = 35
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:parser-epub"))
    implementation(project(":core:parser-pdf"))
    implementation(project(":core:parser-comics"))
    implementation(project(":core:parser-lcp"))
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    implementation("org.jetbrains.kotlinx:kotlinx-io:0.3.8")
}