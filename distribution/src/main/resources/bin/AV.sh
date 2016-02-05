#!/bin/bash

if [ -z "${BIOMEDICUS_HOME}" ]; then
    BIOMEDICUS_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
fi

${BIOMEDICUS_HOME}/bin/runClass.sh org.apache.uima.tools.AnnotationViewerMain

