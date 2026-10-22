FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY backend-api/pom.xml .
COPY backend-api/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
