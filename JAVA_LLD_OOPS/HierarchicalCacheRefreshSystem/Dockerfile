FROM openjdk:17-jdk-alpine
LABEL authors="sathwick"

WORKDIR /app
COPY build/libs/your-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
