plugins {
    kotlin("multiplatform") version "1.3.41"
    `build-scan`
}

allprojects {
    group = "org.godot.native.kotlin"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}