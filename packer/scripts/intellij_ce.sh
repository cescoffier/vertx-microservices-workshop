#!/bin/bash

set -e
set -x

cd ${HOME}
wget https://download.jetbrains.com/idea/ideaIC-2016.3.5-no-jdk.tar.gz
tar zxf ideaIC-2016.3.5-no-jdk.tar.gz
mv idea-IC-163.13906.18 idea
rm ideaIC-2016.3.5-no-jdk.tar.gz

mkdir --parents /home/vertx/.local/share/applications
cat <<EOF > /home/vertx/.local/share/applications/jetbrains-idea-ce.desktop
[Desktop Entry]
Version=1.0
Type=Application
Name=IntelliJ IDEA Community Edition
Icon=/home/vertx/idea/bin/idea.png
Exec=env IDEA_JDK=/home/vertx/jdk "/home/vertx/idea/bin/idea.sh" %f
Comment=The Drive to Develop
Categories=Development;IDE;
Terminal=false
StartupWMClass=jetbrains-idea-ce
EOF

