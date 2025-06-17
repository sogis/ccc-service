FROM openjdk:21

USER root

WORKDIR /home/ccc
COPY ccc /home/ccc

RUN cd /home/ccc && \
    chown -R 1001:0 /home/ccc && \
    chmod -R g+rw /home/ccc && \
    ls -la /home/ccc

USER 1001
EXPOSE 8080

#Log4j 2 CVE-2021-44228
ENV LOG4J_FORMAT_MSG_NO_LOOKUPS=true

CMD java -XX:MaxRAMPercentage=80.0 -jar ccc-service.jar ${CCC_MAX_INACTIVITY:-"-DCCC_MAX_INACTIVITY=${CCC_MAX_INACTIVITY}"} ${CCC_MAX_PAIRING:-"-DCCC_MAX_PAIRING=${CCC_MAX_PAIRING}"} ${CCC_DEBUG:+"--spring.profiles.active=ccc-debug"}
