plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.udlap.controlacademico"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.udlap.controlacademico"
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

    kotlin {
        jvmToolchain(11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ── Firebase ──────────────────────────────────────────────────────────────
    // Todas las librerías de Firebase usan versiones compatibles entre sí.
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Firebase Authentication: maneja login/registro con email y contraseña.
    implementation("com.google.firebase:firebase-auth")

    // Cloud Firestore: base de datos en la nube: guarda usuarios, materias, asistencias y calificaciones.
    implementation("com.google.firebase:firebase-firestore")

    // ── Códigos QR ────────────────────────────────────────────────────────────
    // ZXing Android Embedded: Maneja la GENERACIÓN y el ESCANEO de códigos QR.
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Core de ZXing: motor de bajo nivel que usa la librería de arriba.
    implementation("com.google.zxing:core:3.4.1")

    // ── Testing ───────────────────────────────────────────────────────────────
    //testImplementation(libs.junit)
    //androidTestImplementation(libs.androidx.junit)
    //androidTestImplementation(libs.androidx.espresso.core)
}