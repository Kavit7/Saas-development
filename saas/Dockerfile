# ==============================================================================
# Build Stage
# ==============================================================================
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /build

# Copy Maven Wrapper and POM first to take advantage of layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies offline (cache layer)
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and package application jar
COPY src/ src/
RUN ./mvnw clean package -DskipTests

# ==============================================================================
# Production Runtime Stage
# ==============================================================================
FROM eclipse-temurin:25-jre
WORKDIR /app

# Create unprivileged system user for security
RUN groupadd -r springboot && useradd -r -g springboot -m -d /home/springboot springboot

# Copy the built jar from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Create and grant permissions to uploads directory
RUN mkdir -p /app/uploads && chown -R springboot:springboot /app

USER springboot

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
