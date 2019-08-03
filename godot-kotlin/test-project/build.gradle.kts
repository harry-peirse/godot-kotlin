plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX64("mingw") {
        compilations {
            val main by getting {
                binaries {
                    sharedLib(listOf(DEBUG)) {
                        linkerOpts("-O0")
                    }
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