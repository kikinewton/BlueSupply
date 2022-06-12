FROM openjdk:11.0.4-jre-slim

EXPOSE 5001

RUN mkdir /app

COPY target/build.jar /app/build.jar

ENTRYPOINT ["java", "-jar","/app/build.jar"]