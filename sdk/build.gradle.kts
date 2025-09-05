plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.sdk"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.webrtc)
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
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}