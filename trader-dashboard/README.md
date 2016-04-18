# Trader dashboard

This component is a UI to see what's going on with our trading system. It uses:

1. the portfolio service (event bus service)
2. the quotes sent by the quote generator to display the evolution of the prices.
3. the REST endpoint exposed with the audit service to retrieve the last operations (optional)


## Build

```
mvn clean install docker:build
```

## Run

You can launch the dashboard with or without the audit service.

```
docker run -p 8083:8080 --rm --name dashboard vertx-microservice-workshop/trader-dashboard
# or with the audit
docker run -p 8083:8080 --rm --name dashboard --link audit:AUDIT vertx-microservice-workshop/trader-dashboard
```