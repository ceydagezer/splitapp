plugins {
    kotlin("jvm") version "2.3.21"
    // Spring creates subclasses of our classes at runtime (e.g. for @Transactional).
    // Kotlin classes are `final` by default, so this plugin makes Spring-annotated classes `open`.
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.1"
    // Lets us write dependencies without versions: Spring Boot picks versions that are tested together.
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.splitapp"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    // Web server (embedded Tomcat) + @RestController + JSON
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    // Teaches the JSON library (Jackson) about Kotlin data classes, default values and nullability
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        // Treat Spring's @Nullable annotations as real Kotlin nullability (String? vs String)
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
