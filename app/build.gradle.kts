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
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.itextpdf:itext7-core:7.1.15")
    implementation ("androidx.recyclerview:recyclerview:1.4.0")
    // Giữ nguyên: Cardview (1.0.0 là phiên bản cuối cùng)
    implementation ("androidx.cardview:cardview:1.0.0")

    // --- Thư viện bên thứ ba ---
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.core:core-ktx:1.12.0")
    // CẬP NHẬT: Material Components lên 1.12.0 (Tối ưu hóa cho Theme Material3)
    implementation("com.google.android.material:material:1.12.0")

    // --- Quản lý Firebase (SỬ DỤNG BOM để giải quyết xung đột) ---
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Khai báo các thư viện Firebase MÀ KHÔNG GHI RÕ PHIÊN BẢN (Quản lý bởi BOM):
    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore:24.11.1")
    implementation ("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}