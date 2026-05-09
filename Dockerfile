# Multi-stage build
FROM eclipse-temurin:17-jdk as build
WORKDIR /workspace
COPY pom.xml mvnw .
COPY .mvn .mvn
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
