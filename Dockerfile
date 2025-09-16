# 1단계: Gradle로 빌드
FROM gradle:8.5-jdk17 AS builder
WORKDIR /workspace
COPY . .
RUN ./gradlew clean build -x test

# 2단계: 실행 이미지
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

# 프로파일별 application.yml 주입 (빌드 타임 ARG로 제어)
ARG SPRING_PROFILES_ACTIVE=dev
COPY src/main/resources/application-${SPRING_PROFILES_ACTIVE}.yml application.yml

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
