# CCC-Service betreiben

## Bezug des Image

Das Image ist im Docker Hub unter https://hub.docker.com/r/sogis/ccc-service/ publiziert:

    docker pull sogis/ccc-service

## Starten

    docker run -d -p 8081:8080 --name ccctest sogis/ccc-service

Der ccc-service ist nun unter der URL `ws://localhost:8081/ccc-service` ansprechbar.

## Logs

    docker logs ccctest

## Beenden

    docker kill ccctest
    docker rm ccctest

## Debug-Modus

    docker run -d -p 8080:8080 -e CCC_DEBUG=1 --name ccctest sogis/ccc-service

## Health-Check

    http://localhost:8081/actuator/health
