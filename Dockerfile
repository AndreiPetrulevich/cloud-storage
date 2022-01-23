FROM maven:3.8.3-adoptopenjdk-15 as build

COPY ./src  /usr/src
COPY ./pom.xml /usr

RUN mvn -f /usr/pom.xml clean package

FROM openjdk:15-alpine3.12

COPY --from=build /usr/target/cloud-storage-1.jar /usr/app/cloud-storage-1.jar 
EXPOSE 8190
ENTRYPOINT ["java", "-jar", "/usr/app/cloud-storage-1.jar"]
