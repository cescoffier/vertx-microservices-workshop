# Quote generator

The quote generator simulates the evolution of the values of 3 companies. Every quote is sent on the event bus. It 
also exposes a HTTP endpoint to retrieve the last quote of each company. 


## Build

```
mvn clean install docker:build
```

## Run

```
docker run -p 8081:8080 --rm --name quote-generator vertx-microservice-workshop/quote-generator
```