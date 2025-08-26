plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://mlrepo.djl.ai/") }
}

dependencies {
    ///added google calendar dependencies
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")
    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation("com.google.api-client:google-api-client-gson:2.0.0")
    implementation("com.google.http-client:google-http-client-gson:1.42.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    runtimeOnly("com.h2database:h2")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // DJL BOM
    implementation(platform("ai.djl:bom:0.34.0"))

    // DJL
    implementation("ai.djl:api")
    implementation("ai.djl:model-zoo")

    // DJL PyTorch engine
    runtimeOnly("ai.djl.pytorch:pytorch-engine")
    runtimeOnly("ai.djl.pytorch:pytorch-native-auto")

    // DJL HuggingFace tokenizers
    implementation("ai.djl.huggingface:tokenizers")


}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
