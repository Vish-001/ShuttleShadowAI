FROM eclipse-temurin:21-jdk-alpine

# Install Maven and build dependencies
RUN apk add --no-cache maven

WORKDIR /app

# Copy only the POM first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Run the application (using the exact JAR name from your POM)
ENTRYPOINT ["java", "-jar", "target/ShuttleShadowAI-1.0.0.jar"]
