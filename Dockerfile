# Stage 1: Build with JDK 22
FROM gradle:8.6-jdk22-alpine as builder

WORKDIR /app
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY src src
RUN gradle build -x test --no-daemon

# Stage 2: Run with JRE 22
FROM eclipse-temurin:22-jre-alpine

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Render.com required settings
ENV PORT=8080
EXPOSE $PORT

# Health check and memory limits for Render's free tier
ENV JAVA_OPTS="-Xmx512m -Xms256m"
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --quiet --tries=1 --spider http://localhost:$PORT/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]