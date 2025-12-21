plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.miniproject"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.miniproject"
        minSdk = 24
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Use Paypal for payment
    //implementation("com.paypal.checkout:android-sdk:1.3.2")

// Coroutines (if not already added)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Compose BOM - use a newer version compatible with Stripe
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))

    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.text)
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ========== STRIPE DEPENDENCY ==========
    implementation("com.stripe:stripe-android:22.4.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

// ========== FIREBASE BOM (MANAGES VERSIONS) ==========
    implementation(platform("com.google.firebase:firebase-bom:34.7.0")) // <-- UPDATE THIS

    // ========== FIREBASE DEPENDENCIES ==========
    // These will use versions from BOM
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth") // if using Auth
    implementation("com.google.firebase:firebase-messaging")  // NO VERSION HERE
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-functions")
    implementation("com.google.firebase:firebase-storage")

    // ========== ANDROID NOTIFICATION DEPENDENCIES ==========
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ========== YOUR EXISTING DEPENDENCIES ==========
    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")

    // Lifecycle for observing LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Credential dependencies
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    // QR code generator
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.google.zxing:javase:3.5.1")

    // QR code scanner (ML Kit)
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // Image loading library (Coil)
    implementation("io.coil-kt:coil-compose:2.5.0")
    // Your existing libs dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}