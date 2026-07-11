plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.runtimelabs.clarity"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.runtimelabs.clarity"
        minSdk = 29
        targetSdk = 35
        versionCode = 2
        versionName = "0.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Deliberately no signingConfig here — see
            // .github/workflows/android-build.yml's own header comment:
            // this build stays keystore-free on purpose, and CI signs the
            // resulting app-release-unsigned.apk itself afterward via
            // zipalign + apksigner, reading the keystore from four GitHub
            // Secrets. A local.properties-based signingConfig was added
            // here in an earlier pass without checking for this — genuinely
            // redundant with a more deliberate design already in place, so
            // it's been removed rather than left as two competing ways to
            // do the same thing. To build a signed release locally, follow
            // the same path CI does: `gradle assembleRelease`, then
            // zipalign + apksigner by hand with your own keystore.
            //
            // AGP already defaults this to false for the release build type;
            // stated explicitly so it's never accidentally inherited or
            // overridden by a future build-type tweak for a build type that
            // ships to real users.
            isDebuggable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        // Lets AdsManager branch on BuildConfig.DEBUG to pick the test vs
        // production banner ad unit id from one place (plan: "centralized
        // AdsManager"). Off by default on AGP 8+, so this must be explicit.
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ksp {
    // Room schema export -> versioned migration history lives in /app/schemas.
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    // Core / lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended) // stripped by R8 in release
    implementation(libs.compose.ui.text.google.fonts)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation (type-safe routes via kotlinx.serialization)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Persistence
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.datastore.preferences)

    // SQLCipher (full-database encryption). Version catalog can't express the
    // @aar classifier, so this one is declared inline intentionally.
    implementation("net.zetetic:sqlcipher-android:4.6.1@aar")

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Home-screen widget (Phase D). glance-appwidget alone is sufficient —
    // it transitively brings in core androidx.glance; no glance-material3
    // (the widget hard-codes brand colors instead of Material/dynamic
    // color, see widget/WidgetColors.kt).
    implementation(libs.glance.appwidget)

    // Ads (banner-only, non-sensitive screens; see ads/AdsManager.kt and
    // ARCHITECTURE.md §23 for the privacy trade-off this represents).
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    // Google Play Billing v8 (one-time, non-consumable "remove_ads" product
    // only — no subscriptions). billing-ktx supplies the suspend-function
    // extensions used throughout PlayBillingConnector, keeping purchase
    // code idiomatic Kotlin rather than nested listener callbacks.
    implementation(libs.billing)
    implementation(libs.billing.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
}
