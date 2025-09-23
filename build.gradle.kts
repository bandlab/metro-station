plugins {
    kotlin("multiplatform") version "2.2.0" apply false
    kotlin("jvm") version "2.2.0" apply false
    id("com.github.gmazzo.buildconfig") version "5.6.5"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.16.3" apply false
}

allprojects {
    group = "com.bandlab.metro.station"
    version = "0.1.0-SNAPSHOT"
}
