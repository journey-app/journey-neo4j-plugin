# Journey-neo4j-plugin

**journye-neo4j-plugin** is a neo4j server extension provides out-of-box action path analysis features on top of the graph database. You can use it in areas such as realtime user tracking, convertion funnel analysis etc.

This project is currently under active development. The released artificats is for you to trying out, not for production use yet.

## Installation

* First you need have a neo4j server setup and running correctly. (Currently only support version 2.2.3).
* Download the [latest nightly build](), move it into NEO4J_INSTALL_DIR/plugins.
* Add following line into NEO4j_INSTALL_DIR/config/neo4j-server.properties
```bash
org.neo4j.server.thirdparty_jaxrs_classes=com.thoughtworks.studios.journey=/unmanaged
```
* Restart neo4j server.

## License

![GNU Public License version 3.0](http://www.gnu.org/graphics/gplv3-127x51.png)
**journey-neo4j-plugin** is released under [GNU Public License version 3.0](http://www.gnu.org/licenses/gpl-3.0.txt)


## Copyright

Copyright 2015 ThoughtWorks, Inc. and Pengchao Wang
