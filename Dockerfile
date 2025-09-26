# 1단계: Gradle 빌드
FROM gradle:8.5-jdk17 AS builder
WORKDIR /workspace
COPY . .
RUN ./gradlew clean build -x test

# 2단계: 실행 이미지 (안정적인 Temurin JDK 사용)
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

# 프로파일별 application.yml 복사 (빌드 시 ARG로 제어)
ARG SPRING_PROFILES_ACTIVE=dev
COPY src/main/resources/application-${SPRING_PROFILES_ACTIVE}.yml application.yml

# 포트 노출 (Spring Boot dev 환경에서는 8081)
EXPOSE 8081

# Micrometer CPU metrics 버그 방지 옵션 추가
ENTRYPOINT ["/usr/bin/java", "-Dmanagement.metrics.enable.processor=false", "-jar", "app.jar"]
