# Multi-stage Dockerfile for building and running the Spring Boot application
# Builder stage: uses Maven with JDK 17 to build the application
FROM maven:3.8.8-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy only the files necessary for a Maven build first to leverage Docker layer caching
COPY pom.xml .
COPY src ./src

# Build the project (skip tests for faster builds; remove -DskipTests to run tests)
RUN mvn -B -DskipTests package

# Runtime stage: use a slim OpenJDK 17 image to run the packaged JAR
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built jar from the builder stage; rename to a stable filename
COPY --from=builder /app/target/*.jar app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Use a simple entrypoint to run the app. Accepts additional JVM options via JAVA_OPTS
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

