plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

// URL pública de la API del asistente (MONVORA.Asistente.Api) en Azure App Service
val asistenteApiAzure = "https://monvora-asistente-api-ducgg3effzhkhnbe.brazilsouth-01.azurewebsites.net/"

android {
    namespace = "com.example.appmovimientos"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.appmovimientos"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        debug {
            // API del asistente corriendo en la PC: el emulador la ve como 10.0.2.2
            buildConfigField("String", "ASISTENTE_API_URL", "\"http://10.0.2.2:5080/\"")
        }
        release {
            // API del asistente publicada en Azure (HTTPS)
            buildConfigField("String", "ASISTENTE_API_URL", "\"$asistenteApiAzure\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // Versión para que los compañeros prueben la app en sus celulares:
        // igual que debug (instalable sin configurar firmas), pero usa la API de Azure por HTTPS.
        // Usa la configuración de red de src/main, que solo permite HTTPS.
        create("demo") {
            initWith(getByName("debug"))
            buildConfigField("String", "ASISTENTE_API_URL", "\"$asistenteApiAzure\"")
            versionNameSuffix = "-demo"
            matchingFallbacks += listOf("debug")
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // ✅ Navigation Compose (corregido)
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Test
    testImplementation(libs.junit)
    // org.json real para pruebas unitarias (en Android viene incluido)
    testImplementation("org.json:json:20240303")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //fire base
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")


    // Inicio de sesión con Google
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
}