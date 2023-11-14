FROM openjdk:17-jdk-alpine
ARG JAR_FILE=build/libs/*.jar
COPY build/libs/pet-backend-users-0.0.1-SNAPSHOT.jar pet-backend-users.jar
ENTRYPOINT ["java", "-jar", "/pet-backend-users.jar"]