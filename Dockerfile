# Stage 1: Build with JDK 22
FROM eclipse-temurin:22-jdk-jammy as builder

WORKDIR /app

# Copy Gradle wrapper files first (with execute permission)
COPY --chmod=0755 gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Verify Java and Gradle versions
RUN java --version && \
    ./gradlew --version && \
    ./gradlew dependencies --no-daemon

# Build application
COPY src src
RUN ./gradlew build -x test --no-daemon

# Stage 2: Run with JRE 22
FROM eclipse-temurin:22-jre-jammy

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Render.com required settings
ENV PORT=8080
EXPOSE $PORT
ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]