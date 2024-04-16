
# Registry service (Eureka)

## Description

This service is responsible for registering all the services that are part of the system. It is a Eureka server that will be used by the other services to register themselves.

## How to build the Docker image$

To build the Docker image, you need to run the following command:

```bash
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=eureka-server
```
