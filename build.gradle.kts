plugins {
    id("java")
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

val npmCommand = if (System.getProperty("os.name").lowercase().contains("windows")) "npm.cmd" else "npm"
val frontendDir = layout.projectDirectory.dir("frontend")
val frontendDistDir = frontendDir.dir("dist")
val staticDir = layout.projectDirectory.dir("src/main/resources/static")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

group = "by.michael"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val npmInstall by tasks.registering(Exec::class) {
    group = "frontend"
    description = "Install frontend dependencies"
    workingDir(frontendDir.asFile)
    commandLine(npmCommand, "install")
}

val npmBuild by tasks.registering(Exec::class) {
    group = "frontend"
    description = "Build Vue frontend"
    dependsOn(npmInstall)
    workingDir(frontendDir.asFile)
    commandLine(npmCommand, "run", "build")
}

val cleanFrontendStatic by tasks.registering(Delete::class) {
    group = "frontend"
    description = "Remove existing static assets before copying new frontend build"
    delete(staticDir)
}

val copyFrontendDist by tasks.registering(Copy::class) {
    group = "frontend"
    description = "Copy frontend dist into Spring static resources"
    dependsOn(npmBuild, cleanFrontendStatic)
    from(frontendDistDir)
    into(staticDir)
}

tasks.processResources {
    dependsOn(copyFrontendDist)
}

tasks.test {
    useJUnitPlatform()
}