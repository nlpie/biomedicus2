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

CLASS=${1}
shift

BIOMEDICUS_OPTS="-Dbiomedicus.path.home=${BIOMEDICUS_HOME} ${BIOMEDICUS_OPTS}"

if [ -n "${BIOMEDICUS_CONF}" ]; then
    BIOMEDICUS_OPTS="-Dbiomedicus.path.conf=${BIOMEDICUS_CONF} ${BIOMEDICUS_OPTS}"
fi

$JAVA -Xmx8g $BIOMEDICUS_OPTS $CLASS "$@"
