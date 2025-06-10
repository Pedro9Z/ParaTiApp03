// Módulo app build.gradle.kts - CON HILT CONFIGURADO

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp") // <--- Plugin KSP añadido
    id("com.google.dagger.hilt.android") // <--- Plugin Hilt añadido
}

android {
    namespace = "com.example.paratiapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.paratiapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.04.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.webkit:webkit:1.10.0")

     // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.0.0")) // <-- AÑADE EL BOM (usa la última versión)

    // Declara Firestore KTX SIN versión (el BOM se encarga)
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // Coroutines Tasks/Play Services (para await())
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0") // O la última versión

    // Hilt Core Dependencies
    implementation("com.google.dagger:hilt-android:2.51.1") // Misma versión que en classpath
    ksp("com.google.dagger:hilt-compiler:2.51.1")      // ¡Usar KSP!

    // Hilt Navigation Compose (para inyectar ViewModels en Composables)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0") // O la última versión
}