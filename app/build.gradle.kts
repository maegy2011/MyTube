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
        versionCode = 38
        versionName = "0.3.8"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    // T8 (audit P1 2026-11-11): collectAsStateWithLifecycle for Room queries.
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.exoplayer.hls)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.ui)
    implementation(libs.coil.compose)
    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)
    implementation(libs.coroutines.core)
    implementation(libs.okhttp)
    implementation(libs.newpipe.extractor)
    implementation(libs.play.services.cast.framework)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    // Deferred item 8 (2026-11-11): periodic feed refresh behind a pref (default OFF).
    implementation(libs.work.runtime)
    ksp(libs.room.compiler)
    // P2 tests (audit 2026-11-11): resolve on the first ONLINE sync; compileDebugKotlin needs none of these.
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
}
