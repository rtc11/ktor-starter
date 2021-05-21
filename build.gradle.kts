import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.0"
}

group = "no.tordly.libs"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/ktor")
}

dependencies {
    api(kotlin("reflect"))
    api("io.ktor:ktor-server-netty:1.5.4")

    // Metrics
    api("io.ktor:ktor-metrics-micrometer:1.5.4")
    api("io.micrometer:micrometer-registry-prometheus:1.6.6")

    // Database
    api("org.jetbrains.exposed:exposed:0.17.13")
    api("com.zaxxer:HikariCP:2.7.8")
    api("org.postgresql:postgresql:42.2.20")
    api("org.flywaydb:flyway-core:7.8.2")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("net.logstash.logback:logstash-logback-encoder:6.6") {
        exclude("com.fasterxml.jackson.core")
    }

    // GraphQL
    api("io.ktor:ktor-client-core:1.5.4") // client
    api("io.ktor:ktor-client-cio:1.5.4") // engine
    api("io.ktor:ktor-client-logging:1.5.4") // logging
    api("io.ktor:ktor-jackson:1.5.4")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.3") // JavaTimeModule
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
    withSourcesJar()
}

kotlin.sourceSets {
    main { kotlin.srcDir("src") }
    test { kotlin.srcDir("test") }
}

sourceSets {
    main { java.srcDir("resources") }
}

tasks {
    wrapper {
        gradleVersion = "7.0.1"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "15"
    }
    named<KotlinCompile>("compileTestKotlin") {
        kotlinOptions.jvmTarget = "15"
    }
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showExceptions = true
            showStackTraces = true
            showCauses = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = true
        }
    }
}
