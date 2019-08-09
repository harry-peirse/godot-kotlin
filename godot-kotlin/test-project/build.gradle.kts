plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX64("mingw") {
        compilations {
            val main by getting {
                binaries {
                    sharedLib(listOf(DEBUG))
                }
                dependencies {
                    implementation(project(":lib"))
                }
            }
        }
    }
    sourceSets {
        val mingwMain by getting { }
    }
}