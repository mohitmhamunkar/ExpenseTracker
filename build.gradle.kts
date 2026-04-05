// Top-level build file where you add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.parcelize) apply false

    // Hilt 2.59.2 is correct for AGP 9.0
    id("com.google.dagger.hilt.android") version "2.59.2" apply false

    // THE FIX: Move to KSP 2.3.6+ which explicitly supports AGP 9.0 built-in Kotlin
    id("com.google.devtools.ksp") version "2.3.6" apply false
}