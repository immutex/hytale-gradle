plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    id("com.gradle.plugin-publish") version "1.3.0"
}

group = "com.immutex.hytale"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    website.set("https://github.com/immutex/hytale-gradle")
    vcsUrl.set("https://github.com/immutex/hytale-gradle.git")

    val hytale by plugins.creating {
        id = "com.immutex.hytale"
        implementationClass = "com.immutex.hytale.HytaleGradlePlugin"
        displayName = "Hytale Gradle"
        description = "A Gradle plugin for Hytale that automatically finds the HytaleServer library, and adds a task to run a server with your plugin."
        tags.set(listOf("hytale"))
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.named<Task>("check") {
    dependsOn(functionalTest)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
