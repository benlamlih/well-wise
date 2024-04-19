FROM amazoncorretto:21-alpine

RUN apk --no-cache add curl

COPY ./target/registry-0.0.1-SNAPSHOT.jar /usr/app/eureka-server.jar

WORKDIR /usr/app

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "eureka-server.jar"]
