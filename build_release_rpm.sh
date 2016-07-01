#!/bin/bash

VERSION=0.1
NAME=cdmi-server
TOPDIR=`pwd`/rpm

mvn clean package

cp target/$NAME-$VERSION-SNAPSHOT.jar $TOPDIR/SOURCES
cp config/application.yml $TOPDIR/SOURCES

rpmbuild --define "_topdir ${TOPDIR}" -ba $TOPDIR/SPECS/$NAME.spec

cp ${TOPDIR}/RPMS/x86_64/cdmi-server-0.1-1.x86_64.rpm .
#cp ${TOPDIR}/RPMS/x86_64/cdmi-server-1.0-1.el7.centos.x86_64.rpm .
