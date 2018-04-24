FROM openjdk

USER root

WORKDIR /home/ccc
COPY ccc /home/ccc

RUN cd /home/ccc && \
    chown -R 1001:0 /home/ccc && \
    chmod -R g+rw /home/ccc && \
    ls -la /home/ccc

USER 1001
EXPOSE 8080
CMD java -jar ccc-service.jar ${CCC_DEBUG:+"--spring.profiles.active=ccc-debug"}