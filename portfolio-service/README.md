= Portfolio service


== Build

```
mvn clean install docker:build
```

== Run

```
docker run --name portfolio --rm --link quote-generator:CONSOLIDATION \
 --link some-redis:DISCOVERY_REDIS vertx-microservice-workshop/portfolio-service
```