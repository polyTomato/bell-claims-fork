import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "xyz.mizarc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/central")
    }
    maven {
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("io.mockk:mockk:1.13.7")
    testImplementation("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    testImplementation("com.github.MilkBowl:VaultAPI:1.7")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    shadow("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation ("org.slf4j:slf4j-nop:2.0.7")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("com.github.stefvanschie.inventoryframework:IF:0.10.11")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
}

tasks.shadowJar {
    relocate("co.aikar.commands", "dev.mizarc.bellclaims.acf")
    relocate("co.aikar.locales", "dev.mizarc.bellclaims.locales")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.javaParameters = true
}