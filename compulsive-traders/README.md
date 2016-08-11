# Compulsive Traders

The compulsive traders projects contains 2 implementations of (very dumb) _traders_ that sell and buy shares. They
receive quotes from the event bus and use the portfolio service to buy and sell shared.  

## Build

```
mvn clean package
```

## Run

```
java -jar target/compulsive-traders-1.0-SNAPSHOT-fat.jar
```
