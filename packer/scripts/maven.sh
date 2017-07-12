#!/bin/bash

set -e
set -x

cd ${HOME}
MIRROR=$(curl 'https://www.apache.org/dyn/closer.cgi' |   grep -o '<strong>[^<]*</strong>' |   sed 's/<[^>]*>//g' |   head -1)
wget ${MIRROR}/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
tar zxf apache-maven-3.3.9-bin.tar.gz
mv apache-maven-3.3.9 maven
echo 'export M2_HOME=${HOME}/maven' >> ${HOME}/.bash_profile
echo 'export PATH=${M2_HOME}/bin:${PATH}' >> ${HOME}/.bash_profile
rm apache-maven-3.3.9-bin.tar.gz

