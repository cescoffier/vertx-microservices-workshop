= Portfolio service


== Build

```
mvn clean install docker:build
```

== Run

```
docker run -p 8082:8080 --rm --name audit vertx-microservice-workshop/audit-service
```