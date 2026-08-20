FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# Das Basisimage bringt eine defekte apt-Quelle für Yarn mit
# (/etc/apt/sources.list.d/yarn.list), deren GPG-Key nicht mehr auflösbar ist.
# Das lässt "apt-get update" fehlschlagen und reisst dadurch jede
# devcontainer-Feature-Installation mit, die intern apt-get update ausführt
# (z.B. docker-outside-of-docker). Quelle entfernen, bevor Features gebaut werden.
RUN rm -f /etc/apt/sources.list.d/yarn.list \
    && apt-get update
