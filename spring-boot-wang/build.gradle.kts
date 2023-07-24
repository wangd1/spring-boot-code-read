plugins {
    id("java")
    id("org.springframework.boot.conventions")
}

description = "Spring Boot Wang"

group = "com.wang"
version = "2.7.13"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":spring-boot-project:spring-boot-starters:spring-boot-starter-web"))
    testImplementation(project(":spring-boot-project:spring-boot-starters:spring-boot-starter-test"))

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
