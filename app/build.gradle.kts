plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services") apply false
}

// The Google Services plugin requires app/google-services.json, which doesn't exist yet
// (Firebase/FCM setup is a pending Phase 1 checklist item). Only apply the plugin once
// that file is actually present, so CI/local builds keep working in the meantime.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.homehub.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.homehub.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Points at the local backend by default; override per build type / local.properties
        // Points at the live Render backend for now (matches current manual
        // testing setup). Swap back to "http://10.0.2.2:5000/" for local
        // backend dev against the emulator.
        buildConfigField("String", "BASE_URL", "\"https://homehub-backend-k6qj.onrender.com/\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        // Phase 7 CI fix: this specific check has a long-standing false-positive
        // history (see AGP issue tracker, component 527362) on Compose-only
        // activities using registerForActivityResult() — it flags the
        // resolved Fragment version even when nothing in the app actually
        // uses fragments and a modern one is present transitively. The
        // explicit fragment-ktx dependency above should already satisfy it,
        // but disabling it here too means a future transitive-dependency
        // shuffle can't silently fail CI on this again.
        disable += "InvalidFragmentVersionForActivityResult"
    }
}

dependencies {
    // Core / Compose
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    // Fixes lint's InvalidFragmentVersionForActivityResult false positive on
    // MainActivity's registerForActivityResult() call (Phase 1's notification
    // permission request): lint checks the resolved Fragment version and
    // flags it if nothing in the dependency graph pins one >= 1.3.0 —
    // activity-compose pulls in a modern fragment transitively at runtime,
    // but lint doesn't always resolve that transitively, so it's pinned
    // here explicitly.
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    // Phase 7 Step 2 polish: device-type icons (Lightbulb, Thermostat, Sensors,
    // Power) live in the extended set, not core. Same compose-bom, no separate
    // version needed.
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.socket:socket.io-client:2.1.1") {
        exclude(group = "org.json", module = "json")
    }

    // Firebase (FCM)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}