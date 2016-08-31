# Trader dashboard

This component is a UI to see what's going on with our trading system. It uses:

1. the portfolio service (event bus service)
2. the quotes sent by the quote generator to display the evolution of the prices.
3. the REST endpoint exposed with the audit service to retrieve the last operations (optional)


## Build

```
mvn clean package
```

## Run

```
java -jar target/trader-dashboard-1.0-SNAPSHOT-fat.jar
```
