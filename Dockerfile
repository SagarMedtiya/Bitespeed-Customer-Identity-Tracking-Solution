# Stage 1: Build with JDK 22
FROM eclipse-temurin:22-jdk-alpine as builder

WORKDIR /app
# Install Gradle manually
RUN apk add --no-cache bash
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew --version

COPY src src
RUN ./gradlew build -x test --no-daemon

# Stage 2: Run with JRE 22
FROM eclipse-temurin:22-jre-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Render.com required settings
ENV PORT=8080
EXPOSE $PORT
ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]{PORT}"]