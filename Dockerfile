# Multi-stage build for Smart Parking application
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
ENV APP_REDIS_ENABLED=false
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
