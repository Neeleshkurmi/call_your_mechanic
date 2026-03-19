FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml pom.xml
COPY src src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar
COPY src/main/resources/application.yaml /app/config/application.yaml

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
