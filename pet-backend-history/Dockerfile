FROM openjdk:17-jdk-alpine
ARG JAR_FILE=build/libs/*.jar
COPY build/libs/pet-backend-history-0.0.1-SNAPSHOT.jar pet-backend-history.jar
ENTRYPOINT ["java", "-jar", "/pet-backend-history.jar"]
EXPOSE 8084
