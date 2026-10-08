plugins {
    kotlin("jvm") version "2.3.21"
    // Spring creates subclasses of our classes at runtime (e.g. for @Transactional).
    // Kotlin classes are `final` by default, so this plugin makes Spring-annotated classes `open`.
    kotlin("plugin.spring") version "2.3.21"
    // JPA needs every entity to have an empty constructor; this plugin generates one for @Entity classes.
    kotlin("plugin.jpa") version "2.3.21"
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

    // Database: JPA/Hibernate (objects <-> tables), Flyway (runs our SQL migrations), PostgreSQL driver
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        // Treat Spring's @Nullable annotations as real Kotlin nullability (String? vs String)
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

// Hibernate also creates subclasses of entities (for lazy loading), so they must be `open` too.
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
