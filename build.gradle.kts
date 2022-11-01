import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.gretty") version "3.0.6"
    war
    id("com.vaadin") version "23.2.6"
}

val vaadin_version = "23.2.6"

defaultTasks("clean", "build")

gretty {
    contextPath = "/"
    servletContainer = "jetty9.4"
}

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

    // include proper kotlin version
    implementation(kotlin("stdlib-jdk8"))

    // Vaadin
    implementation("com.vaadin:vaadin-core:${vaadin_version}")
    providedCompile("javax.servlet:javax.servlet-api:4.0.1")

    // adds support for cancelable coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // a http client which does not block
    implementation("org.asynchttpclient:async-http-client:2.12.3")

    implementation("org.slf4j:slf4j-simple:2.0.0")

    // simple REST support so that we can test the REST client
    implementation("io.javalin:javalin:4.6.0") {
        exclude(mapOf("group" to "org.eclipse.jetty"))
        exclude(mapOf("group" to "org.eclipse.jetty.websocket"))
    }

    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v10:1.3.21")
    testImplementation("com.github.mvysny.dynatest:dynatest:0.24")
    testImplementation("io.javalin:javalin:4.6.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        // to see the exceptions of failed tests in Travis-CI console.
        exceptionFormat = TestExceptionFormat.FULL
    }
}

