[![Build Status](https://travis-ci.org/mvysny/vaadin-coroutines-demo.svg?branch=master)](https://travis-ci.org/mvysny/vaadin-coroutines-demo)

# Vaadin Coroutine Demo

Demoes the possibility to use coroutines in a Vaadin app. Please read the [Vaadin and Kotlin Coroutines](http://mavi.logdown.com/posts/3488105)
blogpost for explanation of the ideas behind this project. 

Uses [Karibu-DSL](https://github.com/mvysny/karibu-dsl); for more information about the
Karibu-DSL framework please see https://github.com/mvysny/karibu-dsl .
For more information on Vaadin please see https://vaadin.com/docs/-/part/framework/tutorial.html

# Getting Started

To quickly start the app, make sure that you have Java 8 JDK installed. Then, just type this into your terminal:

```bash
git clone https://github.com/mvysny/vaadin-coroutine-demo
cd vaadin-coroutine-demo
./gradlew build appRun
```

The app will be running on http://localhost:8080/

# Workflow

To compile the entire project, run `./gradlew`.

To run the application, run `./gradlew appRun` and open http://localhost:8080/ .

This will allow you to quickly start the example app and allow you to do some basic modifications.
For real development we recommend Intellij IDEA Ultimate, please see below for instructions.

# Development with Intellij IDEA Ultimate

The easiest way (and the recommended way) to develop Karibu-DSL-based web applications is to use Intellij IDEA Ultimate.
It includes support for launching your project in any servlet container (Tomcat is recommended)
and allows you to debug the code, modify the code and hot-redeploy the code into the running Tomcat
instance, without having to restart Tomcat.

1. First, download Tomcat and register it into your Intellij IDEA properly: https://www.jetbrains.com/help/idea/2017.1/defining-application-servers-in-intellij-idea.html
2. Then just open this project in Intellij, simply by selecting `File / Open...` and click on the
   `build.gradle` file. When asked, select "Open as Project".
2. You can then create a launch configuration which will launch this example app in Tomcat with Intellij: just
   scroll to the end of this tutorial: https://kotlinlang.org/docs/tutorials/httpservlets.html
3. Start your newly created launch configuration in Debug mode. This way, you can modify the code
   and press `Ctrl+F9` to hot-redeploy the code. This only redeploys java code though, to
   redeploy resources just press `Ctrl+F10` and select "Update classes and resources"

