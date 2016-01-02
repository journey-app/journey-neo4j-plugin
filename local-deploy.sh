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
