%define __jar_repack 	%{nil}
%define _tmppath	%{_topdir}/tmp
%define buildroot	%{_topdir}/hello-rpm-root

Name:		cdmi-server	
Version:	1.0
Release:	1%{?dist}
Summary:	SNIA CDMI server reference implementation.

Group:		Applications/Web
License:	apache2
URL:		https://github.com/indigo-dc/CDMI

Requires:	jre >= 1.8

%description
SNIA CDMI server reference implementation.
Standalone Spring Boot application version.

%prep

%build

%install
mkdir -p %{buildroot}/usr/local/bin
mkdir -p %{buildroot}/usr/lib/cdmi-server
cp %{_topdir}/SOURCES/cdmi-server %{buildroot}/usr/local/bin/cdmi-server
cp %{_topdir}/SOURCES/cdmi-server-0.1-SNAPSHOT.jar %{buildroot}/usr/lib/cdmi-server

%files
/usr/local/bin/cdmi-server
/usr/lib/cdmi-server/cdmi-server-0.1-SNAPSHOT.jar

%changelog

