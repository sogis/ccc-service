# Single-stage Dockerfile for existing build process
# This version works with the existing 'jardist' Gradle task
FROM eclipse-temurin:25-jre-alpine

# OCI Labels (will be set by buildImage task in build.gradle)
LABEL org.opencontainers.image.title="CCC-Service"
LABEL org.opencontainers.image.description="CLIENT-CLIENT CONTEXT WebSocket service"
LABEL org.opencontainers.image.vendor="sogis"
LABEL org.opencontainers.image.source="https://github.com/sogis/ccc-service"

WORKDIR /home/ccc

# CVEs reported by scanners that do not affect this application (WebSocket message router,
# no image processing, no font rendering, no shell execution at runtime):
#
# CVE-2025-60876 (busybox 1.37.0-r30, CVSS 6.5)
#   busybox is present in the Alpine base image but is not invoked at runtime.
#   The container runs a JVM process directly (exec java ...) as non-root uid 1001.
#
# CVE-2026-25646 / CVE-2026-40930 (libpng 1.6.57-r0, CVSS 5.4)
#   libpng is a transitive dependency of the Alpine base image.
#   This service processes no image data; libpng is never loaded.
#
# CVE-2026-23865 (freetype 2.14.1-r0, CVSS 5.3)
#   freetype is a transitive dependency of the Alpine base image.
#   This service performs no font rendering; freetype is never loaded.
#
# CVE-2026-34182 (openssl, CVSS 9.1)
#   CMS AuthEnvelopedData authentication bypass. This service does not use
#   OpenSSL CMS/S/MIME; the JVM uses its own TLS stack (JSSE). The system
#   OpenSSL library is never invoked by the application.
#
# CVE-2016-2781 (coreutils 9.8-r1, CVSS 4.6)
#   chroot-related issue in coreutils. The container runs as non-root uid 1001
#   inside an already-isolated namespace; chroot is not called by the application.

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
    -Xlog:gc:stdout:time,level \
    -Djava.security.egd=file:/dev/./urandom \
    -jar ccc-service.jar \
    ${CCC_DEBUG:+"--spring.profiles.active=ccc-debug"}
