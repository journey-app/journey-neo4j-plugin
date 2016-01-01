#!/bin/bash

set -e -x
NEO_HOME=/usr/local/Cellar/neo4j/2.2.3/libexec
PLUGIN_CFG=org.neo4j.server.thirdparty_jaxrs_classes=com.thoughtworks.studios.journey=/unmanaged
NEO_CFG_FILE=$NEO_HOME/conf/neo4j-server.properties

mvn clean install
neo4j stop

grep -q "$PLUGIN_CFG" "$NEO_CFG_FILE" || echo "$PLUGIN_CFG" >> "$NEO_CFG_FILE"

cp target/journey-neo4j-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar $NEO_HOME/plugins/
if [ "$1" = "--clean" ]; then
    rm -rf $NEO_HOME/data/graph.db
fi
neo4j start
