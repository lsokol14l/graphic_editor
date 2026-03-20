plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
    application
}

application {
    mainClass.set("by.michael.Main")
}

group = "by.michael"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.swing")
}

tasks.test {
    useJUnitPlatform()
}