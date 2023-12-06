plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}


android {
    namespace = "com.example.a113project"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.a113project"
        minSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // For View Binding
    viewBinding {
        enable = true
    }

    // 或者對於 Data Binding
    dataBinding {
        enable= true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {
    implementation  ("androidx.core:core-ktx:1.10.0")
    implementation  ("androidx.appcompat:appcompat:1.6.1")
    implementation  ("com.google.android.material:material:1.8.0")
    implementation  ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation  ("androidx.recyclerview:recyclerview:1.3.2")

    implementation ("androidx.camera:camera-camera2:1.2.1")    // CameraX核心庫
    implementation ("androidx.camera:camera-view:1.2.1")    // CameraX視圖庫
    implementation ("androidx.camera:camera-lifecycle:1.2.1")    // CameraX生命週期庫

    implementation  ("com.jakewharton.timber:timber:5.0.1")

    implementation  ("org.jetbrains.kotlin:kotlin-symbol-processing-api:1.4.0-rc-dev-experimental-20200828")

    testImplementation  ("junit:junit:4.13.2")

    androidTestImplementation   ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation   ("androidx.test.espresso:espresso-core:3.5.1")

    kotlin("com.pinterest:ktlint:0.36.0")
}
