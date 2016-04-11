= Portfolio service


== Build

```
mvn clean install docker:build
```

== Run

```
docker run --name portfolio --rm --link quote-generator:CONSOLIDATION \
 vertx-microservice-workshop/portfolio-service
```