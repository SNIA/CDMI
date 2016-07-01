#!/bin/bash

VERSION=0.1
NAME=cdmi-server

mvn clean package

mkdir -p debian/var/lib/$NAME/config/
cp config/application.yml debian/var/lib/$NAME/config/
cp target/$NAME-$VERSION-SNAPSHOT.jar debian/var/lib/$NAME/

dpkg --build debian

mv debian.deb $NAME-$VERSION.deb
