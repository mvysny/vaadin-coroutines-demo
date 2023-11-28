import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version "1.9.21"
    id("application")
    id("com.vaadin")
}

defaultTasks("clean", "build")

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

repositories {
    mavenCentral()
}

dependencies {
    // Karibu-DSL dependency
    implementation("com.github.mvysny.karibudsl:karibu-dsl:2.1.0")
    implementation("com.github.mvysny.vaadin-boot:vaadin-boot:12.2")

    // include proper kotlin version
    implementation(kotlin("stdlib-jdk8"))

    // Vaadin
    implementation("com.vaadin:vaadin-core:${properties["vaadinVersion"]}") {
        afterEvaluate {
            if (vaadin.productionMode) {
                exclude(module = "vaadin-dev")
            }
        }    
    }

    // adds support for cancelable coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")  // https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core/

    implementation("org.slf4j:slf4j-simple:2.0.7")

    // simple REST support so that we can test the REST client
    implementation("io.javalin:javalin:5.6.2") {
        exclude(group = "org.eclipse.jetty")
        exclude(group = "org.eclipse.jetty.websocket")
        exclude(group = "com.fasterxml.jackson.core")
    }

    testImplementation("com.github.mvysny.kaributesting:karibu-testing-v24:2.1.0")
    testImplementation("com.github.mvysny.dynatest:dynatest:0.24")
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
