#!/bin/bash

if [ -z "${BIOMEDICUS_HOME}" ]; then
    BIOMEDICUS_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
fi

BIOMEDICUS_OPTS=-Djava.util.logging.config.file=${BIOMEDICUS_HOME}/config/edu/umn/biomedicus/config/Logger.properties \
-D

${BIOMEDICUS_HOME}/bin/runClass.sh edu.umn.biomedicus.uima.SimpleRunCPE "$@"
