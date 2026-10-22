FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY backend-api/pom.xml .
COPY backend-api/src ./src
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
