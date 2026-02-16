# Single-stage Dockerfile for existing build process
# This version works with the existing 'jardist' Gradle task
FROM eclipse-temurin:25-jre-alpine

# OCI Labels (will be set by buildImage task in build.gradle)
LABEL org.opencontainers.image.title="CCC-Service"
LABEL org.opencontainers.image.description="CLIENT-CLIENT CONTEXT WebSocket service"
LABEL org.opencontainers.image.vendor="sogis"

WORKDIR /home/ccc

# Copy pre-built JAR
COPY ccc/ccc-service.jar .

# Set ownership for OpenShift compatibility
RUN chown -R 1001:0 /home/ccc && \
    chmod -R g+rw /home/ccc &&\
    ls -la /home/ccc

# Run as non-root user
USER 1001

EXPOSE 8080

# Use exec for proper signal handling (critical for graceful shutdown in Kubernetes)
# JVM flags optimized for this workload:
# The CCC-Service is an I/O-bound WebSocket message router with very little garbage:
# - Messages are forwarded as raw strings (no re-serialization)
# - Few concurrent sessions (App+GIS pairs)
# - Most time spent waiting on WebSocket I/O
#
# Memory:
# - MaxRAMPercentage: 80% of container memory
# - UseContainerSupport: Respect container memory limits
# - ExitOnOutOfMemoryError: Fast fail on OOM for K8s to restart the pod
# Garbage Collection:
# - UseSerialGC: Best choice for this workload because:
#     - Very little garbage (raw message forwarding)
#     - No GC threads competing with application threads
#     - No G1 memory overhead (~15-20% saved)
#     - GC pauses are rare and short
#   Switch to G1GC only if: >=1 vCPU, >=1Gi Memory, many concurrent sessions
# Performance:
# - java.security.egd: Faster startup with non-blocking entropy source
# Monitoring:
# - Xlog:gc*: GC logging for Kubernetes monitoring (stdout)
CMD exec java \
    -XX:MaxRAMPercentage=80.0 \
    -XX:+UseContainerSupport \
    -XX:+ExitOnOutOfMemoryError \
    -XX:+UseSerialGC \
    -Xlog:gc*:stdout:time,level,tags \
    -Djava.security.egd=file:/dev/./urandom \
    -jar ccc-service.jar \
    ${CCC_DEBUG:+"--spring.profiles.active=ccc-debug"}
