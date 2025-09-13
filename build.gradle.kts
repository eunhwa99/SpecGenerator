import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("java")
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.asciidoctor.jvm.convert") version "4.0.0"
}

repositories {
	mavenCentral()
}

tasks.named<Jar>("jar") {
	enabled = false
}

tasks.named<BootJar>("bootJar") {
	enabled = false
}


subprojects { // 모든 하위 모듈들에 이 설정을 적용합니다.
	group = "com.example"
	version = "0.0.1-SNAPSHOT"

	apply(plugin = "java")
	apply(plugin = "kotlin")
	apply(plugin = "kotlin-spring")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")
	apply(plugin = "org.asciidoctor.jvm.convert")

	java {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}

	kotlin {
		compilerOptions {
			jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
			freeCompilerArgs.addAll("-Xjsr305=strict")
		}
	}
	configurations {
		compileOnly.get().extendsFrom(configurations.annotationProcessor.get())
	}

	repositories {
		mavenCentral()
	}

	dependencies {
		implementation("org.springframework.boot:spring-boot-starter")
		implementation("org.jetbrains.kotlin:kotlin-reflect")
		testImplementation("org.springframework.boot:spring-boot-starter-test")
		testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
		testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	}

	tasks.named<Test>("test") {
		useJUnitPlatform()
	}
}