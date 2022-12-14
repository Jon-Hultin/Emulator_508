plugins {
    id("java")
}

group = "com.jagex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "java")

    dependencies {
        implementation("org.slf4j:slf4j-api:2.0.0-beta1")
        implementation("org.slf4j:slf4j-jdk14:2.0.0-beta1")
        implementation("com.google.guava:guava:31.1-jre")
        implementation("io.netty:netty-all:4.1.79.Final")
    }

}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}