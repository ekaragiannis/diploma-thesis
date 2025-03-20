# Use a Maven image with Java 21 for the build stage
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and source code into the container
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean package

# Use an OpenJDK 21 image for the runtime stage
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged jar file from the build stage
COPY --from=build /app/target/kafka-streams-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

# Define the command to run the application
CMD ["java", "-jar", "app.jar"]