#!/bin/bash
#
# This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provides out-of-box action path analysis features on top of the graph database.
#
# Copyright 2015 ThoughtWorks, Inc. and Pengchao Wang
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#


set -x
mvn clean install
ssh -i ~/.ssh/mingle-dev.pem ec2-user@$NEOSERVER "sudo service neo4j stop"
scp -i ~/.ssh/mingle-dev.pem target/journey-neo4j-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar ec2-user@$NEOSERVER:/usr/share/neo4j-community-2.2.3/plugins
ssh -i ~/.ssh/mingle-dev.pem ec2-user@$NEOSERVER "sudo service neo4j start"
