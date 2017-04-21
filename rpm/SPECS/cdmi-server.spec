%define __jar_repack 	%{nil}
%define _tmppath	%{_topdir}/tmp
%define buildroot	%{_topdir}/build-rpm-root

%define name            cdmi-server
%define jarversion      1.2
%define user            cdmi

Name:		%{name}
Version:	%{jarversion}
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
mkdir -p %{buildroot}/usr/local/man/man8
mkdir -p %{buildroot}/var/lib/%{name}/config
mkdir -p %{buildroot}/etc/systemd/system
mkdir -p %{buildroot}/usr/lib/%{name}/plugins
cp %{_topdir}/SOURCES/cdmi-server.8.gz %{buildroot}/usr/local/man/man8
cp %{_topdir}/SOURCES/application.yml %{buildroot}/var/lib/%{name}/config
cp %{_topdir}/SOURCES/%{name}-%{jarversion}.jar %{buildroot}/var/lib/%{name}
cp %{_topdir}/SOURCES/%{name}.service %{buildroot}/etc/systemd/system

%files
/usr/local/man/man8/cdmi-server.8.gz
/var/lib/%{name}/config/application.yml
/var/lib/%{name}/%{name}-%{jarversion}.jar
/etc/systemd/system/%{name}.service
%dir /usr/lib/%{name}/plugins

%changelog

%post
/usr/bin/id -u %{user} > /dev/null 2>&1
if [ $? -eq 1 ]; then
  adduser --system --user-group %{user}
fi

if [ -f /var/lib/%{name}/%{name}-%{jarversion}.jar ]; then
  chmod +x /var/lib/%{name}/%{name}-%{jarversion}.jar
fi

if [ -f /usr/local/man/man8/cdmi-server.8.gz ]; then
  chmod 0644 /usr/local/man/man8/cdmi-server.8.gz
fi

chown -R %{user}:%{user} /var/lib/%{name}

systemctl start %{name}.service
systemctl enable %{name}.service
