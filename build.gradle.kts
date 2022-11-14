import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "1.7.20"
    id("application")
    id("com.vaadin") version "23.2.8"
}

val vaadin_version = "23.2.8"

defaultTasks("clean", "build")

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    // Karibu-DSL dependency
    implementation("com.github.mvysny.karibudsl:karibu-dsl:1.1.3")
    implementation("com.github.mvysny.karibu-tools:karibu-tools:0.11")
    implementation("com.github.mvysny.vaadin-boot:vaadin-boot:10.1")

    // include proper kotlin version
    implementation(kotlin("stdlib-jdk8"))

    // Vaadin
    implementation("com.vaadin:vaadin-core:${vaadin_version}")

    // adds support for cancelable coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // a http client which does not block
    implementation("org.asynchttpclient:async-http-client:2.12.3")

    implementation("org.slf4j:slf4j-simple:2.0.0")

    // simple REST support so that we can test the REST client
    implementation("io.javalin:javalin:4.6.7") {
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.eclipse.jetty.websocket")
        exclude(group = "com.fasterxml.jackson.core")
    }

    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v10:1.3.21")
    testImplementation("com.github.mvysny.dynatest:dynatest:0.24")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        // to see the exceptions of failed tests in Travis-CI console.
        exceptionFormat = TestExceptionFormat.FULL
    }
}

application {
    mainClass.set("org.test.MainKt")
}
