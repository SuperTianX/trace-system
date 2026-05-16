# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: Run with JRE
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/target/trace-system.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
