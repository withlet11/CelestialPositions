// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    // val kotlin_version by extra("2.0.21")
    // val compose_version by extra("1.5.15")

    repositories {
        google()
        mavenCentral()
        // jcenter()
    }
    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files

        classpath(libs.oss.licenses.plugin)
        classpath(libs.androidx.compiler)
    }
}

plugins {
    alias(libs.plugins.compose.compiler)
    // id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}