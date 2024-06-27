FROM openjdk:20-jdk-slim-buster
COPY ./AuthProject-0.0.1-SNAPSHOT.jar ./AuthProject.jar

ENTRYPOINT ["java", "-jar", "./AuthProject.jar"]
