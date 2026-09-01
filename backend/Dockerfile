# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Render injects PORT; the app reads ${PORT} (defaults to 8081 locally).
EXPOSE 8081
# Modest heap + SerialGC to fit Render's 512MB free tier.
ENTRYPOINT ["sh", "-c", "java -Xms128m -Xmx320m -XX:+UseSerialGC -jar app.jar"]
