plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}

buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
