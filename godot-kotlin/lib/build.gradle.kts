plugins {
    kotlin("multiplatform")
}

kotlin {
    mingwX64("mingw") {
        compilations {
            val main by getting {
                outputKinds(STATIC)

                val godotapi by cinterops.creating {}
            }
        }
    }
    sourceSets {
        val mingwMain by getting {}
    }
}