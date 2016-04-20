# INDIGO-DataCloud CDMI Server

This project ports the SNIA CDMI-Server reference implementation to a Spring Boot application.

## Requirements

* JDK 1.8+
* [Maven 3+](https://maven.apache.org/)

## Build & Run

The project uses maven build automation tool that will build one fat jar Spring Boot application.

```
mvn clean package
```

The CDMI server can run without any additional server deployment (Tomcat, JBoss, etc.).

To specify the server port use the ```--server.port=9000``` parameter, default 8080.

```
java -jar cdmi-server-0.0.1-SNAPSHOT.jar --server.port=9000
```

Configuration for the application is done in the application.properties file.
Configuration for the CDMI capabilities (QoS) is done in capabilities.properties.json

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
