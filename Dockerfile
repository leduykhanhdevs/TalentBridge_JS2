# ==========================================
# Stage 1: Build JAR using Eclipse Temurin JDK 21
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Import the public Amazon RDS CA roots into a dedicated truststore. The JDBC
# URL can then use sslMode=VERIFY_IDENTITY without replacing the JVM-wide
# truststore used by other outbound HTTPS clients.
RUN wget -qO /tmp/rds-region-bundle.pem https://truststore.pki.rds.amazonaws.com/ap-southeast-1/ap-southeast-1-bundle.pem \
    && awk '/-----BEGIN CERTIFICATE-----/ { cert++; file=sprintf("/tmp/rds-ca-%02d.pem", cert) } file != "" { print > file } /-----END CERTIFICATE-----/ { close(file); file="" }' /tmp/rds-region-bundle.pem \
    && for certificate in /tmp/rds-ca-*.pem; do \
        keytool -importcert -noprompt -trustcacerts \
            -alias "$(basename "$certificate" .pem)" \
            -file "$certificate" \
            -keystore /tmp/rds-ca-truststore.p12 \
            -storetype PKCS12 \
            -storepass changeit; \
    done \
    && keytool -list -keystore /tmp/rds-ca-truststore.p12 -storetype PKCS12 -storepass changeit >/dev/null

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

# Pre-create uploads directory and grant ownership to non-root appuser
RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app

# Copy executable jar from builder stage
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar
COPY --from=builder --chown=appuser:appgroup /tmp/rds-ca-truststore.p12 /app/rds-ca-truststore.p12

# Switch to non-root user
USER appuser

# Expose server port
EXPOSE 8080

# Configure JVM options for low memory environments (Render Free Tier 512MB RAM)
# - SerialGC: Lowest memory overhead for single-core / low-memory containers
# - MaxRAMPercentage=70.0: Limits heap to ~350MB, leaving ~160MB for metaspace & thread stacks
# - Xss256k: Reduces thread stack from default 1MB to 256KB
ENV JAVA_OPTS="-XX:+UseSerialGC -XX:MaxRAMPercentage=70.0 -Xss256k -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

