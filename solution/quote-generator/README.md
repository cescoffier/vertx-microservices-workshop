= Quote generator


== Build

```
mvn clean install docker:build
```

== Run

```
docker run -p 8081:8080 --rm --name quote-generator vertx-microservice-workshop/quote-generator
```