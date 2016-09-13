#!/bin/bash

VERSION=0.2
NAME=cdmi-server

mvn clean package

mkdir -p debian/var/lib/$NAME/config/
cp config/application.yml debian/var/lib/$NAME/config/
cp target/$NAME-$VERSION.jar debian/var/lib/$NAME/

dpkg --build debian

mv debian.deb $NAME-${VERSION}_all.deb
