plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.example.client"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.client"
        minSdk = 24
        targetSdk = 35
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
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose.jvmstubs)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Compose
    implementation (platform("androidx.compose:compose-bom:2023.08.00")) // Используйте актуальную версию
    implementation ("androidx.compose.ui:ui")
    implementation ("androidx.compose.ui:ui-graphics")
    implementation ("androidx.compose.ui:ui-tooling-preview")
    implementation ("androidx.compose.material3:material3")

    // Activity & Fragment KTX для by viewModels()
    implementation ("androidx.activity:activity-ktx:1.9.0") // Используйте актуальную версию
    implementation ("androidx.fragment:fragment-ktx:1.7.0" )// Если используете фрагменты

    // Lifecycle (ViewModel)
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0") // Используйте актуальную версию
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0") // Используйте актуальную версию
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0") // Для viewModel() в Composables

    // Navigation Compose
    implementation ("androidx.navigation:navigation-compose:2.8.0-beta01") // Используйте актуальную версию

    // Ktor
    implementation ("io.ktor:ktor-client-core:2.3.9") // Используйте актуальную версию
    implementation ("io.ktor:ktor-client-android:2.3.9")
    implementation ("io.ktor:ktor-client-logging:2.3.9")
}