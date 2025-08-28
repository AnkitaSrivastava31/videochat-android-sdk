plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
//    id ("com.google.gms.google-services")
}

android {
    namespace = "com.example.sampleapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sampleapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // AndroidX + UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Local SDK module
    implementation(project(":sdk"))

    // ✅ WebRTC (latest community maintained build)
    implementation("io.github.webrtc-sdk:android:125.6422.08")

    // ✅ OkHttp (WebSocket + HTTP)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
//    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ✅ Retrofit (if you later add REST signaling)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // ✅ Animations (optional but useful for UI)
    implementation("com.airbnb.android:lottie:6.0.0")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.22")

    implementation (platform("com.google.firebase:firebase-bom:33.3.0")) // use latest BoM
    implementation (libs.firebase.storage)
    implementation (libs.firebase.auth)
    implementation (libs.google.firebase.database)
    implementation(libs.google.webrtc)
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
