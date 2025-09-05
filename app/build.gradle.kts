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

    // WebRTC (community maintained)
    implementation("io.github.webrtc-sdk:android:125.6422.08")

    // OkHttp (WebSocket + logging)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Retrofit (for REST signaling if needed)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Animations / UI extras
    implementation("com.airbnb.android:lottie:6.0.0")
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.22")

    // Firebase (via BoM, resolves versions automatically)
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
