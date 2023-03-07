import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "1.8.0"
    id("application")
    id("com.vaadin") version "23.3.6"
}

val vaadin_version = "23.3.6"

defaultTasks("clean", "build")

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

repositories {
    mavenCentral()
}

dependencies {
    // Karibu-DSL dependency
    implementation("com.github.mvysny.karibudsl:karibu-dsl:1.2.0")
    implementation("com.github.mvysny.karibu-tools:karibu-tools:0.14")
    implementation("com.github.mvysny.vaadin-boot:vaadin-boot:10.3")

    // include proper kotlin version
    implementation(kotlin("stdlib-jdk8"))

    // Vaadin
    implementation("com.vaadin:vaadin-core:${vaadin_version}")

    // adds support for cancelable coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // a http client which does not block
    implementation("org.asynchttpclient:async-http-client:2.12.3")

    implementation("org.slf4j:slf4j-simple:2.0.6")

    // simple REST support so that we can test the REST client
    implementation("io.javalin:javalin:4.6.7") {
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.eclipse.jetty.websocket")
        exclude(group = "com.fasterxml.jackson.core")
    }

    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v23:1.3.23")
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
