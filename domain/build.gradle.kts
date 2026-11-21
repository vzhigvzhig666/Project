import extension.configureTargets

plugins {
    alias(libs.plugins.escodro.multiplatform)
    jacoco
}

kotlin {
    configureTargets("domain")

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.logging)
            implementation(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }

    androidLibrary {
        namespace = "com.escodro.domain"
    }
}

jacoco {
    toolVersion = "0.8.14"
}

val desktopTest by tasks.existing(Test::class)

tasks.register<JacocoReport>("domainCoverage") {
    dependsOn(desktopTest)
    executionData(desktopTest.get())
    classDirectories.setFrom(layout.buildDirectory.dir("classes/kotlin/desktop/main"))
    sourceDirectories.setFrom(file("src/commonMain/kotlin"), file("src/desktopMain/kotlin"))
    reports {
        html.required.set(true)
        html.outputLocation.set(rootProject.layout.projectDirectory.dir("reports/coverage"))
        xml.required.set(true)
        xml.outputLocation.set(rootProject.layout.projectDirectory.file("reports/coverage.xml"))
    }
}
