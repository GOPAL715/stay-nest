# ===== Build Stage =====
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache dependencies separately from source code
COPY pom.xml .
RUN mvn dependency:resolve -q

COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -q

# ===== Run Stage =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S staynest && adduser -S staynest -G staynest

COPY --from=builder /app/target/api-1.0.0.jar app.jar

RUN chown staynest:staynest app.jar
USER staynest

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
