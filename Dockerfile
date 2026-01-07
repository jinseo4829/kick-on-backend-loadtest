FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드 결과 JAR 복사
COPY build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-Dspring.profiles.active=loadtest","-jar","/app/app.jar"]
