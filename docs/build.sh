#!/bin/bash

# Run using:
#
# ./convert.sh
#
# or
#
# ./convert.sh html,pdf
#
# ...where the first argument is a comma-delimited list of formats

# Program paths
ASCIIDOCTOR=asciidoctor
FOPUB=fopub
ASCIIDOCTOR_PDF=asciidoctor-pdf

# File names
MASTER_ADOC=index.adoc
MASTER_DOCBOOK=${MASTER_ADOC/.adoc/.xml}

# Command options
mkdir -p output
SHARED_OPTIONS='-a toc=left -a stylesheet! -a numbered -a experimental -a source-highlighter=prettify -r asciidoctor-diagram -a imagesdir=images
--destination-dir=output'

cp -R images output
cp -R assets output

echo "Converting to HTML ..."
$ASCIIDOCTOR -v $SHARED_OPTIONS $MASTER_ADOC

cp -R images output
mv chapters/*.png images
rm -Rf **/.asciidoctor


exit 0