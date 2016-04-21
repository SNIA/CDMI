# INDIGO-DataCloud CDMI Server

This project ports the SNIA CDMI-Server reference implementation to a Spring Boot application.

## Requirements

* JDK 1.8+
* [Maven 3+](https://maven.apache.org/)

## Build & Run & Configure

The project uses maven build automation tool that will build one fat jar Spring Boot application.

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

To specify the data root directory use the ```--cdmi.data.baseDirectory=DIR``` parameter, default "data" (relative to the jar execution).

```
java -jar cdmi-server-0.0.1-SNAPSHOT.jar --cdmi.data.baseDirectory=/cdmi/data
```

All configuration for the application can be done either via command line parameters or in the application.properties file.

Configuration for the CDMI capabilities (QoS) is done in capabilities.properties.json

For example to add a new container class insert following into the capabilities.properties.json file and rebuild.

```
{ ...
...
container-capabilities:
{
...
ssd: {
    cdmi_list_children: true,
	cdmi_read_metadata: true,
	cdmi_modify_metadata: true,
	cdmi_create_dataobject: true,
	cdmi_post_dataobject: false,
	cdmi_create_container: true,
	cdmi_copy_dataobject: true,
	cdmi_qos_accesslatency: 10, 
    cdmi_qos_durability: 95, 
    cdmi_qos_price: 3
},
...
```

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
