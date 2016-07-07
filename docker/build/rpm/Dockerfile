FROM ubuntu:trusty

MAINTAINER KIT-SCC

# Install prerequisites
RUN apt-get update
RUN apt-get install -y software-properties-common git rpm

# Install java8
RUN add-apt-repository -y ppa:webupd8team/java
RUN apt-get update
RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java8-installer

# Install maven
RUN apt-get install -y maven

# Cone and build
RUN git clone https://github.com/indigo-dc/CDMI 
RUN mkdir -p /build

CMD cd CDMI && git pull && ./build_release_rpm.sh && cp *.rpm /build

# Expose build folder
VOLUME /build
