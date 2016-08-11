# Audit service

The audit service receives operation (shares bought or sold) from the event bus and store them in a database. It also
 provides a REST endpoint to retrieve the last 10 operations.

## Build

```
mvn clean package
```

## Run

```
java -jar target/audit-service-1.0-SNAPSHOT-fat.jar
```
