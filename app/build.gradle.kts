plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.gestiondeinventario"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.gestiondeinventario"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    
    // CameraX
    implementation("androidx.camera:camera-core:${libs.versions.camerax.get()}")
    implementation("androidx.camera:camera-camera2:${libs.versions.camerax.get()}")
    implementation("androidx.camera:camera-lifecycle:${libs.versions.camerax.get()}")
    implementation("androidx.camera:camera-view:${libs.versions.camerax.get()}")
    implementation("androidx.camera:camera-mlkit-vision:${libs.versions.camerax.get()}")
    
    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:${libs.versions.mlkitBarcode.get()}")

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}