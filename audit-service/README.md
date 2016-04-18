# Audit service

The audit service receives operation (shares bought or sold) from the event bus and store them in a database. It also
 provides a REST endpoint to retrieve the last 10 operations.

## Build

```
mvn clean install docker:build
```

## Run

```
docker run -p 8082:8080 --rm --name audit vertx-microservice-workshop/audit-service
```