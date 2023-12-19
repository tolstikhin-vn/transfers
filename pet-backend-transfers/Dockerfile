FROM openjdk:17-jdk-alpine
ARG JAR_FILE=build/libs/*.jar
COPY build/libs/pet-backend-transfers-0.0.1-SNAPSHOT.jar pet-backend-transfers.jar
ENTRYPOINT ["java", "-jar", "/pet-backend-transfers.jar"]
EXPOSE 8083