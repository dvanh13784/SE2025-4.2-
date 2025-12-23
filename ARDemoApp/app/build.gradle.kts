plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.ardemoapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.ardemoapp"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- CÁC THƯ VIỆN CÓ SẴN (GIỮ NGUYÊN) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // --- CÁC THƯ VIỆN BẠN THÊM VÀO (ĐÃ SỬA CHUẨN) ---

    // 1. Thư viện AR Sceneform (Bản chuẩn của Gorisse - thay thế bản Google cũ)
    implementation("com.gorisse.thomas.sceneform:sceneform:1.23.0")

    // 2. Thư viện Appcompat (Để sửa lỗi AppCompatActivity đỏ)
    implementation("androidx.appcompat:appcompat:1.6.1")

    // 3. Thư viện OkHttp (Để gọi Server - Giữ lại bản 4.9.3 mới nhất)
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}