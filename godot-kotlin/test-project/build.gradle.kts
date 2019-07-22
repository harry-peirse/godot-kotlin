plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX64("mingw") {
        compilations {
            val main by getting {
                binaries {
                    sharedLib()
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