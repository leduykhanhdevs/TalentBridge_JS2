# ==========================================
# Stage 1: Build JAR using Eclipse Temurin JDK 21
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven Wrapper and POM first to leverage layer cache
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies offline to optimize cache
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build production package
COPY src/ src/
RUN ./mvnw clean package -DskipTests=true

# ==========================================
# Stage 2: Minimal Production JRE Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner

# Create non-root system user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy executable jar from builder stage
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose server port
EXPOSE 8080

# Configure JVM options and entry point
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
