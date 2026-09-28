plugins {
    id("com.android.application")
}

android {
    namespace = "com.nyusamma.a16"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nyusamma.a16"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "0.4-checkpoint"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
