# 构建文档

使用以下命令构建文档:

* 使用 Fish:

    docker run -it -v (pwd):/documents/ asciidoctor/docker-asciidoctor "./build.sh" "html,pdf"

* 使用 Bash

    docker run -it -v `pwd`:/documents/ asciidoctor/docker-asciidoctor "./build.sh" "html,pdf"
