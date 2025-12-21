import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.21"
    application
    alias(libs.plugins.vaadin)
}

defaultTasks("clean", "build")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget = JvmTarget.JVM_17
}

repositories {
    mavenCentral()
}

dependencies {
    // Karibu-DSL dependency
    implementation(libs.karibu.dsl)
    implementation(libs.vaadin.boot)

    // Vaadin
    implementation(libs.vaadin.core) {
        if (vaadin.effective.productionMode.get()) {
            exclude(module = "vaadin-dev")
        }
    }

    // adds support for cancelable coroutines
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.slf4j.simple)

    // simple REST support so that we can test the REST client
    implementation(libs.javalin) {
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.eclipse.jetty.websocket")
        exclude(group = "com.fasterxml.jackson.core")
    }

    testImplementation(libs.junit)
    testImplementation(libs.karibu.testing)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        // to see the exceptions of failed tests in the CI console.
        exceptionFormat = TestExceptionFormat.FULL
    }
}

application {
    mainClass.set("org.test.MainKt")
}
