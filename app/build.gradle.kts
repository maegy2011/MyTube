import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.ksp)
}

// Phase 1 (2026-11-13): release signing. Secret lives OUTSIDE the repo:
// env RELEASE_STORE_PASSWORD first, then ~/.cellclaw/mytube-family.pw (chmod 600).
// Fail-fast: a missing secret must break the build, never ship unsigned.
val releaseStorePassword: String? = System.getenv("RELEASE_STORE_PASSWORD")
    ?: runCatching {
        File("/home/administrator/.cellclaw/mytube-family.pw").takeIf { it.isFile }?.readText()?.trim()?.ifBlank { null }
    }.getOrNull()
val hasReleaseKey: Boolean = !releaseStorePassword.isNullOrEmpty()
// CI overrides (release.yml sets these from repo secrets; local defaults = family key).
val releaseStoreFile: String = System.getenv("RELEASE_STORE_FILE")
    ?.takeIf { it.isNotBlank() } ?: "/home/administrator/.cellclaw/mytube-family.keystore"
val releaseKeyAlias: String = System.getenv("RELEASE_KEY_ALIAS")
    ?.takeIf { it.isNotBlank() } ?: "family"
val releaseKeyPassword: String? = System.getenv("RELEASE_KEY_PASSWORD")
    ?.takeIf { it.isNotBlank() } ?: releaseStorePassword
android {
    namespace = "com.mohamedadel.mytube"
    compileSdk = 36
    signingConfigs {
        create("release") {
            // CI-safe: debug/CI evaluation never needs the family key.
            // Fail-fast only when a Release task is actually requested (below).
            if (hasReleaseKey) {
                storeFile = file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }
    defaultConfig {
        applicationId = "com.mohamedadel.mytube"
        minSdk = 26
        targetSdk = 36
        versionCode = 42
        versionName = "0.4.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug { isMinifyEnabled = false }
    }
    // Fail-fast (2026-11-13 fix): a Release build without the family secret must
    // break loudly — but debug/CI evaluation configures fine without it.
    val wantsRelease = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
    if (wantsRelease && !hasReleaseKey) throw GradleException("RELEASE_STORE_PASSWORD env or ~/.cellclaw/mytube-family.pw missing — refusing unsigned release")
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
    implementation(libs.media3.cast)
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
