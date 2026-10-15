plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "com.luminaris.extraction"
version = "1.0.0-SNAPSHOT"
description = "Unified Vanilla Extraction Core Engine (L.O.V.E)"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    testImplementation("io.papermc.paper:paper-api:26.3.build.+")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(25)
        options.compilerArgs.addAll(listOf("--enable-preview"))
    }

    compileKotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            )
        }
    }

    shadowJar {
        archiveClassifier.set("")
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
    }

    processResources {
        filteringCharset = "UTF-8"
        val properties = mapOf(
            "version" to project.version,
            "description" to project.description
        )
        inputs.properties(properties)
        filesMatching("paper-plugin.yml") {
            expand(properties)
        }
    }

    test {
        useJUnitPlatform()
    }

    build {
        dependsOn(shadowJar)
    }
}
