import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.0"
    // need to use Gretty here because of https://github.com/johndevs/gradle-vaadin-plugin/issues/317
    id("org.gretty") version "2.2.0"
    id("com.devsoap.plugin.vaadin") version "1.4.1"
}

gretty {
    contextPath = "/"
    servletContainer = "jetty9.4"
}

vaadin {
    version = "8.6.0"
}

defaultTasks("clean", "build")

repositories {
    jcenter()
    maven { setUrl("https://dl.bintray.com/mvysny/github") }
}

val staging by configurations.creating

dependencies {
    // Karibu-DSL dependency
    compile("com.github.mvysny.karibudsl:karibu-dsl-v8:0.5.0")

    // include proper kotlin version
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // workaround until https://youtrack.jetbrains.com/issue/IDEA-178071 is fixed
    compile("com.vaadin:vaadin-themes:${vaadin.version}")
    compile("com.vaadin:vaadin-client-compiled:${vaadin.version}")

    // since we're using async stuff, we need to push updated UI to the client
    compile("com.vaadin:vaadin-push:${vaadin.version}")
    // adds support for cancelable coroutines
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.1")
    // a http client which does not block
    compile("org.asynchttpclient:async-http-client:2.0.37")

    compile("ch.qos.logback:logback-classic:1.2.3")

    // simple REST support so that we can test the client
    compile("org.jboss.resteasy:resteasy-servlet-initializer:3.1.3.Final")

    // heroku app runner
    staging("com.github.jsimone:webapp-runner:9.0.11.0")

    testCompile("com.github.mvysny.kaributesting:karibu-testing-v8:1.0.0")
    testCompile("com.github.mvysny.dynatest:dynatest-engine:0.11")
    testCompile("io.javalin:javalin:2.3.0")
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
