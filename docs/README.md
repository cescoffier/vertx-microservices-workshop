# Build

To build the documentation:

* Using Fish:

```
docker run -it -v (pwd):/documents/ asciidoctor/docker-asciidoctor "./build.sh" "html"
```

* Using Bash

```
docker run -it -v $(pwd):/documents/ asciidoctor/docker-asciidoctor "./build.sh" "html"
```
    
    
