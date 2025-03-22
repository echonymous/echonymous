# Use Maven with JDK 21 to build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and install dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the entire source code
COPY . .

# Build the application (skip tests as required)
RUN mvn clean install -Dmaven.test.skip=true

# Use JDK 21 for running the application
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the backend port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
