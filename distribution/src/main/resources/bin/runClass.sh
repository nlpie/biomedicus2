#!/bin/bash

JAVA=`which java`
[ -n "${JAVA_HOME}" ] && JAVA=${JAVA_HOME}/bin/java

BIOMEDICUS_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"

BIOMEDICUS_CLASSPATH="lib/*"

if [ "x$BIOMEDICUS_LOG4J_CONF" = "x" ]; then
    BIOMEDICUS_LOG4J_CONF="logs/logging.xml"
fi

JAVA_OPTS="$JAVA_OPTS -Xmx12g"
JAVA_OPTS="$JAVA_OPTS -Dlog4j.configurationFile=$BIOMEDICUS_LOG4J_CONF"
JAVA_OPTS="$JAVA_OPTS -Dorg.apache.uima.logger.class=org.apache.uima.util.impl.Log4jLogger_impl"

$JAVA $JAVA_OPTS $BIOMEDICUS_JAVA_OPTS -cp "$BIOMEDICUS_CLASSPATH" $@
