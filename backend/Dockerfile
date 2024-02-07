# Build stage with Maven
FROM openjdk:21-jdk-oracle as maven

RUN microdnf install maven -y

FROM maven as build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn package -DskipTests



# Run stage with JDK 21
FROM openjdk:21-jdk-oracle

# Copy the built jar file from the build stage
COPY --from=build /app/target/*.jar /usr/app/app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","/usr/app/app.jar"]