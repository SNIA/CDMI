#!/bin/bash

TOPDIR=`pwd`/rpm

mvn clean package

cp target/cdmi-server-0.1-SNAPSHOT.jar $TOPDIR/SOURCES

rpmbuild --define "_topdir ${TOPDIR}" -ba $TOPDIR/SPECS/cdmi-server.spec

cp ${TOPDIR}/RPMS/x86_64/cdmi-server-1.0-1.el7.centos.x86_64.rpm .
