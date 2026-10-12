plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.android.library")
}

android {
    namespace = "com.openbooks.parser.comics"
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
    implementation("com.github.junrar:junrar:2.0.1")
    implementation("org.apache.commons:commons-compress:1.27.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-io:0.3.8")
}