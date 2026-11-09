plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mohamedadel.mytube"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.mohamedadel.mytube"
        minSdk = 26
        targetSdk = 34
        versionCode = 23
        versionName = "0.2.3"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug { isMinifyEnabled = false }
    }
    buildFeatures { compose = true; buildConfig = true }
    // offline CI: lint-gradle artifact isn't in the dependency cache — skip release lint
    lint { checkReleaseBuilds = false; abortOnError = false }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)
    implementation(libs.coil.compose)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    implementation(libs.coroutines.core)
    implementation(libs.okhttp)
    implementation(libs.newpipe.extractor)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
