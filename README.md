# INDIGO-DataCloud CDMI Server

This project ports the SNIA CDMI-Server reference implementation to a Spring Boot application.

## Requirements

* JDK 1.8+
* [Maven 3+](https://maven.apache.org/)
* (optional) [Redis](http://redis.io/)

## Build & Run & Configure

The project uses the Maven build automation tool that will build one fat jar Spring Boot application.

It depends on the cdmi-spi Java SPI [cdmi-spi](https://github.com/indigo-dc/cdmi-spi) library.

The cdmi-spi library is provided at http://cdmi-qos.data.kit.edu/maven/ and should be included automatically.

```
mvn clean package
```

**(optional)** if you have problems you can also install the cdmi-spi-\<VERSION\>.jar into your local Maven repository, e.g.

```
mvn install:install-file -Dfile=cdmi-spi-<VERSION>.jar
```

The CDMI server can run without any additional server deployment (Tomcat, JBoss, etc.).

The (default) built jar is executable and can be linked as an init.d service script, see [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#deployment-initd-service)

To run the server run
```
./target/cdmi-server-1.1.jar
```

To specify the server port use the ```--server.port=PORT``` parameter, default "8080".

```
./target/cdmi-server-1.1.jar --server.port=9000
```

To specify the data root directory use the ```--cdmi.data.baseDirectory=DIR``` parameter, default "/tmp/cdmi".

```
./target/cdmi-server-1.1.jar --cdmi.data.baseDirectory=/cdmi/data
```

All configuration for the application can be done either via command line parameters or in the config/application.yml file or any other supported way, see [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-external-config)

It is recommended to run the application behind a https proxy with e.g. apache,nginx ...

The CDMI-Server can be used with two different profiles for storing metadata:

1) using redis db
* (active per default) to use this configuration you have to set the spring.profiles.active attribute in the configuration to "redis"
* the CDMI server comes with an embedded redis db so you don't need to do anything, however it is recommended to use an external redis db

2) using '.' files
* to use this configuration you have to set the spring.profiles.active attribute in the configuration to "filesystem"
* the metadata files will be stored on the filesystem directly (default .cdmi_<objectname>)

## Tests
**Note:** put proper authorization credentials to the requests below or configure the application appropriate.

Some curl commands for testing:

Create container "testcontainer" with metadata "tag=test"
```
curl -u restadmin:restadmin -X PUT http://localhost:8080/testcontainer -H "Content-Type: application/cdmi-container" -d '{"metadata":{"tag":"test"}}'
```
Read container info by path
```
curl -u restadmin:restadmin http://localhost:8080/testcontainer
```
Read container info by objectid
```
curl -u restadmin:restadmin http://localhost:8080/cdmi_objectid/<OBJECT_ID>
```
Read system capabilities
```
curl -u restadmin:restadmin http://localhost:8080/cdmi_capabilities
```
Read container capabilities
```
curl -u restadmin:restadmin http://localhost:8080/cdmi_capabilities/container
```
