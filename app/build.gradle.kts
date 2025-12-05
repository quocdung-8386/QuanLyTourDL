plugins {
    alias(libs.plugins.android.application)
    id ("com.google.gms.google-services")
}

android {
    namespace = "com.example.quanlytourdl"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.quanlytourdl"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // --- Thư viện AndroidX Core ---
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("androidx.cardview:cardview:1.0.0")

    // --- Thư viện bên thứ ba ---
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.android.material:material:1.11.0") // Đã khắc phục lỗi thuộc tính

    // --- Quản lý Firebase (SỬ DỤNG BOM để giải quyết xung đột) ---
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Khai báo các thư viện Firebase MÀ KHÔNG GHI RÕ PHIÊN BẢN:
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-analytics")

    // SỬA LỖI: Sử dụng BOM cho Realtime Database
    implementation ("com.google.firebase:firebase-database")

    // TÙY CHỌN: Nếu bạn không dùng Firestore, nên xóa dòng này:
    implementation ("com.google.firebase:firebase-firestore")

    // TÙY CHỌN: Nếu không dùng Google Sign-In, có thể xóa dòng này:
    implementation ("com.google.android.gms:play-services-auth:21.0.0")

    // --- Test dependencies ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}