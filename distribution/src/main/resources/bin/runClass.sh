#!/bin/bash

JAVA=`which java`
[ -n "${JAVA_HOME}" ] && JAVA=${JAVA_HOME}/bin/java

if [ -z "${BIOMEDICUS_HOME}" ]; then
    BIOMEDICUS_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/.."
fi

if [ -z "${CLASSPATH}" ]; then
    CLASSPATH="${BIOMEDICUS_HOME}/lib/*"
else
    CLASSPATH="${BIOMEDICUS_HOME}/lib/*:${CLASSPATH}"
fi

export CLASSPATH

${JAVA} $@
