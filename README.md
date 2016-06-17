# INDIGO-DataCloud CDMI Server

This project ports the SNIA CDMI-Server reference implementation to a Spring Boot application.

## Requirements

* JDK 1.8+
* [Maven 3+](https://maven.apache.org/)
* cdmi-spi-\<VERSION\>.jar
* 
## Build & Run & Configure

The project uses the Maven build automation tool that will build one fat jar Spring Boot application.

It depends on the cdmi-spi Java SPI [cdmi-spi](https://github.com/indigo-dc/cdmi-spi) library.

You should first install the cdmi-spi-\<VERSION\>.jar into your local Maven repository, e.g.

```
mvn install:install-file -Dfile=cdmi-spi-0.0.1-SNAPSHOT.jar
```

```
mvn clean package
```

The CDMI server can run without any additional server deployment (Tomcat, JBoss, etc.).

```
java -jar cdmi-server-0.0.1-SNAPSHOT.jar
```

To specify the server port use the ```--server.port=PORT``` parameter, default "8080".

```
java -jar cdmi-server-0.0.1-SNAPSHOT.jar --server.port=9000
```

To specify the data root directory use the ```--cdmi.data.baseDirectory=DIR``` parameter, default "test" (relative to the jar execution).

```
java -jar cdmi-server-0.0.1-SNAPSHOT.jar --cdmi.data.baseDirectory=/cdmi/data
```

All configuration for the application can be done either via command line parameters or in the application.properties file.

It is recommended to run the application behind a https proxy with e.g. apache,nginx ...

## Tests

Some curl commands for testing:

Create container "testcontainer" with metadata "tag=test"
```
curl -X PUT http://localhost:8080/testcontainer -H "Content-Type: application/cdmi-container" -d '{"metadata":{"tag":"test"}}'
```
Read container info by path
```
curl http://localhost:8080/testcontainer
```
Read container info by objectid
```
curl http://localhost:8080/cdmi_objectid/<OBJECT_ID>
```
Read system capabilities
```
curl http://localhost:8080/cdmi_capabilities
```
Read container capabilities
```
curl http://localhost:8080/cdmi_capabilities/container
```
