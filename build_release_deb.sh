#!/bin/bash

mvn clean package

mkdir -p debian/usr/lib/cdmi-server
cp target/cdmi-server-0.1-SNAPSHOT.jar debian/usr/lib/cdmi-server

dpkg --build debian

mv debian.deb cdmi-server-1.0.deb
