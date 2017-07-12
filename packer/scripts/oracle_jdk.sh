#!/bin/bash

echo "#################################################################"
echo "Installing Oracle JDK"
echo "By using this script you agree to the Oracle licensing agreement."
echo "#################################################################"

set -e
set -x

cd ${HOME}
wget --no-check-certificate --no-cookies --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u112-b15/jdk-8u112-linux-x64.tar.gz
tar zxf jdk-8u112-linux-x64.tar.gz
mv jdk1.8.0_112 jdk
echo 'export JAVA_HOME=${HOME}/jdk' >> ${HOME}/.bash_profile
echo 'export PATH=${JAVA_HOME}/bin:${PATH}' >> ${HOME}/.bash_profile
rm jdk-8u112-linux-x64.tar.gz

