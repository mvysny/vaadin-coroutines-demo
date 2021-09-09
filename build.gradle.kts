import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    id("org.gretty") version "3.0.6"
    war
    id("com.vaadin") version "0.14.6.0"
}

val vaadin_version = "14.6.8"

defaultTasks("clean", "build")

gretty {
    contextPath = "/"
    servletContainer = "jetty9.4"
}

vaadin {
    if (gradle.startParameter.taskNames.contains("stage")) {
        productionMode = true
    }
    pnpmEnable = true
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

val staging by configurations.creating

dependencies {
    // Karibu-DSL dependency
    implementation("com.github.mvysny.karibudsl:karibu-dsl:1.1.0")

    // include proper kotlin version
    implementation(kotlin("stdlib-jdk8"))

    // Vaadin 14
    implementation("com.vaadin:vaadin-core:${vaadin_version}") {
        // Webjars are only needed when running in Vaadin 13 compatibility mode
        listOf("com.vaadin.webjar", "org.webjars.bowergithub.insites",
                "org.webjars.bowergithub.polymer", "org.webjars.bowergithub.polymerelements",
                "org.webjars.bowergithub.vaadin", "org.webjars.bowergithub.webcomponents")
                .forEach { exclude(group = it) }
    }
    providedCompile("javax.servlet:javax.servlet-api:3.1.0")

    // adds support for cancelable coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    // a http client which does not block
    implementation("org.asynchttpclient:async-http-client:2.12.3")

    implementation("org.slf4j:slf4j-simple:1.7.32")

    // simple REST support so that we can test the REST client
    implementation("io.javalin:javalin:3.13.10") {
        exclude(mapOf("group" to "org.eclipse.jetty"))
        exclude(mapOf("group" to "org.eclipse.jetty.websocket"))
    }

    // heroku app runner
    staging("com.heroku:webapp-runner:9.0.36.1")

    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v10:1.3.2")
    testImplementation("com.github.mvysny.dynatest:dynatest-engine:0.20")
    testImplementation("io.javalin:javalin:3.13.10")
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
