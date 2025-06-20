# Use Maven with Java 21 for the build stage
FROM maven:3.9-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy only the pom.xml first and download dependencies
COPY kafka-streams-app/pom.xml ./
RUN mvn dependency:go-offline

# Now copy the source code (this prevents re-downloading dependencies if code changes)
COPY kafka-streams-app/src ./src

# Package the application
RUN mvn clean package

# Use OpenJDK 21 for the runtime
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy the packaged JAR from the build stage
COPY --from=build /app/target/kafka-streams-app-1.0-SNAPSHOT.jar app.jar

# Run the application
CMD ["java", "-jar", "app.jar"]