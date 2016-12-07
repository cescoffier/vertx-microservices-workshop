How to build individual modules without installing parent project
==================================================================

The [Vertx From Zero To (Micro)Hero](http://escoffier.me/vertx-hol/#_let_s_start) tutorial begins the coding portion by having you clone this repository and then run `mvn clean install` from the root directory. This will install all project dependencies in your local Maven repo (`~/.m2/repositories`) and build the common library (containing only utils). However, this will also install jars of each of the project's modules, which are only partially completed, at least until you have filled in the TODO sections of the java source files.

If you would like to avoid packaging and installing incomplete versions of these modules on your system, it's possible to only install the common classes in `vertx-workshop-common` and the parent project's pom file, so that you are able to build each of the other modules one-by-one as you complete the relevant code sections (which the tutorial systematically guides you through).

After cd'ing into `vertx-workshop-common` and running `mvn clean install`, these files are installed in the local user's Maven repository:
```
.../.m2/repository/io/vertx/workshop/vertx-workshop-common/1.0-SNAPSHOT/_remote.repositories
.../.m2/repository/io/vertx/workshop/vertx-workshop-common/1.0-SNAPSHOT/maven-metadata-local.xml
.../.m2/repository/io/vertx/workshop/vertx-workshop-common/1.0-SNAPSHOT/vertx-workshop-common-1.0-SNAPSHOT.jar
.../.m2/repository/io/vertx/workshop/vertx-workshop-common/1.0-SNAPSHOT/vertx-workshop-common-1.0-SNAPSHOT.pom
.../.m2/repository/io/vertx/workshop/vertx-workshop-common/1.0-SNAPSHOT/vertx-workshop-common-1.0-SNAPSHOT-sources.jar
.../.m2/repository/io/vertx/workshop/vertx-workshop-common/1.0-SNAPSHOT/vertx-workshop-common-1.0-SNAPSHOT-tests.jar
.../.m2/repository/io/vertx/workshop/vertx-workshop-common/maven-metadata-local.xml
```

Create a new directory in the Maven repository:

`.../.m2/repository/io/vertx/workshop/vertx-microservice-workshop/1.0-SNAPSHOT/`

... then copy the parent pom file for this project into that directory, giving it the name `vertx-microservice-workshop-1.0-SNAPSHOT.pom`:

`.../.m2/repository/io/vertx/workshop/vertx-microservice-workshop/1.0-SNAPSHOT/vertx-microservice-workshop-1.0-SNAPSHOT.pom`

Now you can run `mvn clean package` within the other modules and all the maven dependencies will resolve without error.
