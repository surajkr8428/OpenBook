plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.android.library")
}

android {
    namespace = "com.openbooks.parser.pdf"
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
    implementation("org.apache.pdfbox:pdfbox:3.0.4")
    implementation("androidx.pdf:pdf-viewer:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}