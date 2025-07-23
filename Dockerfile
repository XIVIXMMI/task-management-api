FROM amazoncorretto:21.0.4-alpine3.18
WORKDIR app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} task-management.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","task-management.jar"]
