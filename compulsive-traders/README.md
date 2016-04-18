# Compulsive Traders

The compulsive traders projects contains 2 implementations of (very dumb) _traders_ that sell and buy shares. They 
receive quotes from the event bus and use the portfolio service to buy and sell shared.  

## Build

```
mvn clean install docker:build
```

## Run

```
docker run --rm --name traders vertx-microservice-workshop/compulsive-traders
```