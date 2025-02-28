import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	java
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
}

tasks.named<BootJar>("bootJar") {
	archiveBaseName.set("kick-on")
	archiveFileName.set("kick-on.jar")
	archiveVersion.set("0.0.1")
}

group = "kr.kickon"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot 기본 스타터
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// http Client
	compileOnly("org.springframework.boot:spring-boot-starter-webflux")
	implementation("io.netty:netty-resolver-dns-native-macos:4.1.68.Final:osx-aarch_64")


	// Lombok (코드 자동 생성)
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// MySQL 연결 (JDBC 드라이버)
	runtimeOnly("com.mysql:mysql-connector-j")

	// Spring Boot 설정 프로세서 (자동 완성 지원)
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	// QueryDSL (JPA + 코어)
	implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
	implementation("com.querydsl:querydsl-core")
	annotationProcessor("jakarta.annotation:jakarta.annotation-api")
	annotationProcessor("jakarta.persistence:jakarta.persistence-api")
	annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
	implementation(group = "org.javassist", name = "javassist", version = "3.27.0-GA")

	// Hibernate 관련 추가 설정
	implementation("org.hibernate:hibernate-core:6.2.7.Final")

	// 스웨거
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.3")

	// auth
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

	// jwt
	implementation("io.jsonwebtoken:jjwt:0.12.5")

	//AWS Parameter Store
	implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:3.1.0"))
	implementation("io.awspring.cloud:spring-cloud-aws-starter-parameter-store")
	implementation(platform("software.amazon.awssdk:bom:2.27.21"))
	implementation("software.amazon.awssdk:ssm")

	// test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// QueryDSL QClass 생성 디렉토리 설정
val generated = file("src/main/generated")

tasks.withType<JavaCompile> {
	options.generatedSourceOutputDirectory.set(generated)
}

sourceSets {
	main {
		java {
			srcDirs.add(generated)
		}
	}
}

tasks.compileJava {
	dependsOn("clean")
}

tasks.named("clean") {
	delete(generated)
}