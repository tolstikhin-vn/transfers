FROM openjdk:17-jdk-alpine
ARG JAR_FILE=build/libs/*.jar
COPY build/libs/pet-backend-accounts-0.0.1-SNAPSHOT.jar pet-backend-accounts.jar
ENTRYPOINT ["java", "-jar", "/pet-backend-accounts.jar"]