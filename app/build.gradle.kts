plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

}

android {
    namespace = "enlaceschaco.ar"
    compileSdk = 34

    defaultConfig {
        applicationId = "enlaceschaco.ar"
        minSdk = 21
        targetSdk = 34
        versionCode = 4
        versionName = "1.3"
        
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11" // versión estable y compatible con Compose 1.6.8
    }

}

dependencies {

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.runtime)

      // Glide + Landscapist (para Compose)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.skydoves:landscapist-glide:2.3.6")
    
    // Resolución de conflictos de versiones
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib:1.9.23")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.23")
            force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.23")
        }
    }


    // AndroidX Core y Lifecycle
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.multidex:multidex:2.0.1")


    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
