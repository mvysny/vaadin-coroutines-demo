import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.gretty") version "3.0.6"
    war
    id("com.vaadin") version "23.0.5"
}

val vaadin_version = "23.0.5"

defaultTasks("clean", "build")

gretty {
    contextPath = "/"
    servletContainer = "jetty9.4"
}

vaadin {
    if (gradle.startParameter.taskNames.contains("stage")) {
        productionMode = true
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven { setUrl("https://maven.vaadin.com/vaadin-prereleases") }
}

val staging by configurations.creating

dependencies {
    // Karibu-DSL dependency
    implementation("com.github.mvysny.karibudsl:karibu-dsl:1.1.1")
    implementation("com.github.mvysny.karibu-tools:karibu-tools:0.10")

    // include proper kotlin version
    implementation(kotlin("stdlib-jdk8"))

    // Vaadin
    implementation("com.vaadin:vaadin-core:${vaadin_version}") {
        exclude(module = "fusion-endpoint") // exclude fusion: it brings tons of dependencies (including swagger)
    }
    providedCompile("javax.servlet:javax.servlet-api:4.0.1")

    // adds support for cancelable coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    // a http client which does not block
    implementation("org.asynchttpclient:async-http-client:2.12.3")

    implementation("org.slf4j:slf4j-simple:1.7.35")

    // simple REST support so that we can test the REST client
    implementation("io.javalin:javalin:4.3.0") {
        exclude(mapOf("group" to "org.eclipse.jetty"))
        exclude(mapOf("group" to "org.eclipse.jetty.websocket"))
    }

    // heroku app runner
    staging("com.heroku:webapp-runner:9.0.52.1")

    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v10:1.3.12")
    testImplementation("com.github.mvysny.dynatest:dynatest:0.24")
    testImplementation("io.javalin:javalin:4.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        // to see the exceptions of failed tests in Travis-CI console.
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

// Heroku
tasks {
    val copyToLib by registering(Copy::class) {
        into("$buildDir/server")
        from(staging) {
            include("webapp-runner*")
        }
    }
    val stage by registering {
        dependsOn("build", copyToLib)
    }
}
