# Vert.x - From zero to (micro-) hero.

This repository is a lab about vert.x explaining how to build distributed _microservice_ reactive applications using 
Vert.x.

Instructions are available on http://209.132.178.63/workshop/ (this url is going to change)

## Teasing

Vert.x is a toolkit to create reactive distributed applications running on the top of the Java Virtual Machine. Vert.x 
exhibits very good performances, and a very simple and small API based on the asynchronous, non-blocking 
development model.  With vert.x, you can developed microservices in Java, but also in JavaScript, Groovy, Ruby and 
Ceylon. Vert.x also lets you interact with Node.JS, .NET or C applications.  

This lab is an introduction to microservice development using Vert.x. The application is a fake _trading_ 
application, and maybe you are going to become (virtually) rich! The applications is deployed in a set of 
interconnected docker containers.
 
## Content
 
 * Vert.x
 * Microservices
 * Asynchronous non-blocking development model
 * Composition of async operations
 * Distributed event bus
 * Database access
 * Providing and Consuming REST APIs
 * Async RPC on the event bus
 * Microservice discovery

## Want to improve this lab ?

Forks and PRs are definitely welcome !

## Building

To build the code:

    mvn clean install
    
To build the documentation:
    
    cd docs
    docker run -it -v `pwd`:/documents/ asciidoctor/docker-asciidoctor "./build.sh" "html"
    # or for fish
    docker run -it -v (pwd):/documents/ asciidoctor/docker-asciidoctor "./build.sh" "html"
          