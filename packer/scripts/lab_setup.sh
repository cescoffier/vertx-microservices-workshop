#!/bin/bash

set -e
set -x

export JAVA_HOME=${HOME}/jdk
export PATH=${JAVA_HOME}/bin:${PATH}
export M2_HOME=${HOME}/maven
export PATH=${M2_HOME}/bin:${PATH}

echo "#################################################################"
echo "Checking Maven install"
echo
mvn -version
echo
echo "Done"
echo "#################################################################"

cd ${HOME}
export BRANCH=master
git clone https://github.com/cescoffier/vertx-microservices-workshop.git
cd vertx-microservices-workshop
mvn install dependency:go-offline
