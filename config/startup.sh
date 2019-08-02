#!/bin/sh

# -----------------------------------------------------------------------------
# Start Script for the CATALINA Server
# -----------------------------------------------------------------------------

export JAVA_OPTS=-Dport.http="${PORT}"
rm -rf /usr/local/tomcat/webapps/ROOT

./bin/catalina.sh run