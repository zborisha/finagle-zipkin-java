FROM anapsix/alpine-java:jdk8

MAINTAINER zborisha

RUN apk update && apk upgrade
RUN apk add bash

RUN mkdir -p /opt/zborisha/service/

ADD ./startService.sh /opt/zborisha/service/
ADD ./target/finagle-java-0.0.1-SNAPSHOT.jar /opt/zborisha/service/
RUN chmod +x /opt/zborisha/service/startService.sh

EXPOSE 8910 8910

ENV SHELL /bin/bash

ENTRYPOINT ["/opt/zborisha/service/startService.sh"]