plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.giorgioarmaniapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.giorgioarmaniapp"
        minSdk = 26
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
        viewBinding = true
        dataBinding = true
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.core.splashscreen)

    // AAR support
//    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation(files("libs/API3_CMN-release-2.0.5.224.aar"))
    implementation(files("libs/API3_ASCII-release-2.0.5.224.aar"))
    implementation(files("libs/API3_READER-release-2.0.5.224.aar"))
    implementation(files("libs/API3_INTERFACE-release-2.0.5.224.aar"))
    implementation(files("libs/API3_TRANSPORT-release-2.0.5.224.aar"))
    implementation(files("libs/API3_NGE-protocolrelease-2.0.5.224.aar"))
    implementation(files("libs/API3_NGE-Transportrelease-2.0.5.224.aar"))
    implementation(files("libs/API3_NGEUSB-Transportrelease-2.0.5.224.aar"))
    implementation(files("libs/rfidhostlib.aar"))
    implementation(files("libs/rfidseriallib.aar"))
    implementation(files("libs/BarcodeScannerLibrary.aar"))


    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.code.gson:gson:2.10.1")
}
