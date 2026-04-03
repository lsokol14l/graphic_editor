plugins {
    id("java")
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

val bunCommand = if (System.getProperty("os.name").lowercase().contains("windows")) "bun.exe" else "bun"
val frontendDir = layout.projectDirectory.dir("frontend")
val frontendDistDir = frontendDir.dir("dist")
val frontendNodeModulesDir = frontendDir.dir("node_modules")
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

val frontendInstall by tasks.registering(Exec::class) {
    group = "frontend"
    description = "Install frontend dependencies via Bun"
    workingDir(frontendDir.asFile)
    commandLine(bunCommand, "install")

    // Run only when dependency descriptors change or node_modules is missing.
    inputs.file(frontendDir.file("package.json").asFile)
    inputs.file(frontendDir.file("bun.lock").asFile)
    outputs.dir(frontendNodeModulesDir.asFile)
}

val frontendBuild by tasks.registering(Exec::class) {
    group = "frontend"
    description = "Build Vue frontend"
    dependsOn(frontendInstall)
    workingDir(frontendDir.asFile)
    commandLine(bunCommand, "run", "build")
}

val cleanFrontendStatic by tasks.registering(Delete::class) {
    group = "frontend"
    description = "Remove existing static assets before copying new frontend build"
    delete(staticDir)
}

val copyFrontendDist by tasks.registering(Copy::class) {
    group = "frontend"
    description = "Copy frontend dist into Spring static resources"
    dependsOn(frontendBuild, cleanFrontendStatic)
    from(frontendDistDir)
    into(staticDir)
}

tasks.processResources {
    dependsOn(copyFrontendDist)
}

tasks.test {
    useJUnitPlatform()
}