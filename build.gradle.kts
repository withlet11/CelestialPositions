// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlin_version by extra("2.0.21")
    val compose_version by extra("1.5.15")

    repositories {
        google()
        mavenCentral()
        // jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files

        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
        classpath("androidx.compose.compiler:compiler:1.5.15")
    }
}

plugins {
    alias(libs.plugins.compose.compiler)
}

allprojects {
    repositories {
        google()
        mavenCentral()
        // jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}