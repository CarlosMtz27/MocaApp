import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

// Leer local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.cadev.mocaapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cadev.mocaapp"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "CLOUDINARY_CLOUD_NAME",
            "\"${localProperties["CLOUDINARY_CLOUD_NAME"]}\""
        )
        buildConfigField(
            "String",
            "CLOUDINARY_API_KEY",
            "\"${localProperties["CLOUDINARY_API_KEY"]}\""
        )
        buildConfigField(
            "String",
            "CLOUDINARY_API_SECRET",
            "\"${localProperties["CLOUDINARY_API_SECRET"]}\""
        )

        // OneSignal
        buildConfigField("String", "ONESIGNAL_APP_ID",
            "\"${localProperties["ONESIGNAL_APP_ID"]}\"")
        buildConfigField("String", "ONESIGNAL_REST_KEY",
            "\"${localProperties["ONESIGNAL_REST_KEY"]}\"")

        manifestPlaceholders["onesignal_app_id"] =
            localProperties["ONESIGNAL_APP_ID"] ?: ""
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
        viewBinding = true
        compose = true
        buildConfig = true
    }

}

dependencies {
    // Depende de los otros módulos
    implementation(project(":core"))
    implementation(project(":feature"))
    implementation(project(":widgets"))

    // Firebase (para el Factory que ensambla todo)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))

    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    // Compose
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation(libs.androidx.runtime)
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.onesignal:OneSignal:[5.0.0, 5.99.99]")

    // Glance & DataStore
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.glance:glance-material3:1.1.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
