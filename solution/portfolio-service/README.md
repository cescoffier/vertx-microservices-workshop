# Portfolio service

The portfolio service manages your portfolio: the available cash and the owned shares. It is exposed as an async RPC
service on the event bus. It consumes the _consolidation_ endpoint from the quote generator and on every successful
operation, it sends a message on the event bus.


## Build

```
mvn clean package
```

## Run

```
java -jar target/portfolio-service-1.0-SNAPSHOT-fat.jar
```
