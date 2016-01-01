#!/bin/bash

set -x
NEOSERVER='remote-address'
mvn clean install
ssh -i ~/.ssh/mingle-dev.pem ec2-user@$NEOSERVER "sudo service neo4j stop"
scp -i ~/.ssh/mingle-dev.pem target/journey-neo4j-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar ec2-user@$SERVER:/usr/share/neo4j-community-2.2.3/plugins
ssh -i ~/.ssh/mingle-dev.pem ec2-user@$NEOSERVER "sudo service neo4j start"
