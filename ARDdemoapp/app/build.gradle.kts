plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Không cần Compose cho AR + XML + AppCompat
    // alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.arddemoapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.arddemoapp"
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

    // AR demo dùng XML/View, tắt Compose để tránh xung đột template
    buildFeatures {
        compose = false
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.fragment:fragment:1.8.3")

    // ARCore + Sceneform Community
    implementation("com.google.ar:core:1.43.0")
    implementation("com.gorisse.thomas.sceneform:ux:1.23.0")

    // Retrofit stack
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
