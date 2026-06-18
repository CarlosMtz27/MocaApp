plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    id("org.jetbrains.kotlin.plugin.compose")

}

android {
    namespace = "com.cadev.mocaapp.feature"
    compileSdk = 36

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }



}

dependencies {
    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.animation.core)
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.compose.foundation:foundation")



    // Firebase — solo los que cada feature usa
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")       // auth
    implementation("com.google.firebase:firebase-firestore-ktx")  // auth, diario, eventos
    implementation("com.google.firebase:firebase-storage-ktx")    // diario (fotos/videos)
    implementation("com.google.firebase:firebase-messaging-ktx")  // eventos (notificaciones)

    // Coroutines (para .await() de Firebase)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Cargar imágenes desde URL (diario, perfil)
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation(project(":core"))
    implementation("androidx.compose.material:material-icons-extended:1.6.2")

    //Para el storage
    implementation("com.cloudinary:cloudinary-android:2.3.1")

    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.compose.material3:material3:...")

    // Glance & DataStore
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.glance:glance-material3:1.1.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(libs.play.services.location)






}