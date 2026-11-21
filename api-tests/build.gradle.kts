plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.test.junit)
    testImplementation("io.ktor:ktor-client-cio:2.3.11")
    testImplementation(libs.kotlinx.serialization)
}
