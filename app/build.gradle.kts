plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {

    signingConfigs {
        create("release") {
            storeFile = file(System.getProperty("user.home") + "/subkiller-release.keystore")
            storePassword = "SubKiller2026"
            keyAlias = "subkiller"
            keyPassword = "SubKiller2026"
        }
    }

    namespace = "com.subkiller.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.subkiller.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Java 8+ API desugaring for java.time on minSdk 24
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // AndroidX & UI
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Lifecycle & Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Room Database (KSP)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // WorkManager (Background reminders)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Google Mobile Ads (AdMob)
    implementation("com.google.android.gms:play-services-ads:23.2.0")
}
