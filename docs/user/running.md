# CCC-Service betreiben

## Bezug des Image

Das Image ist im Docker Hub unter https://hub.docker.com/r/sogis/ccc-service/ publiziert:

    docker pull sogis/ccc-service

## Starten

    docker run -d -p 8080:8080 --name ccctest sogis/ccc-service

Der ccc-service ist nun unter der URL `ws://localhost:8080/ccc-service` ansprechbar.

## Logs

    docker logs ccctest

## Beenden

    docker kill ccctest
    docker rm ccctest

## Debug-Modus

    docker run -d -p 8080:8080 -e CCC_DEBUG=1 --name ccctest sogis/ccc-service

## Health-Check

    http://localhost:8080/actuator/health

## Lokale Entwicklung / Testing

Für das lokale Bauen und Testen eines Images aus dem aktuellen Quellstand stehen zwei Gradle-Tasks zur Verfügung.

### Image bauen und Container starten

    ./gradlew runImage

Die Task baut das Jar (`jardist`), erstellt daraus ein lokales Docker-Image (`buildImage`, getaggt als `sogis/ccc-service:latest` und `sogis/ccc-service:<version>`) und startet es als Container namens `ccctest` im Debug-Modus auf Port 8080. Die Version ergibt sich aus `cccVersion` in `gradle.properties` plus Buildnummer (lokal: `<cccVersion>.localbuild`).

Nach dem Start ist der Dienst wie gewohnt unter `ws://localhost:8080/ccc-service` erreichbar; Logs via `docker logs ccctest`.

### Container stoppen und entfernen

    ./gradlew removeTestContainer

Stoppt den laufenden `ccctest`-Container und entfernt ihn anschliessend. Fehler (z. B. Container existiert nicht) werden ignoriert, sodass die Task idempotent aufgerufen werden kann.

Vor einem erneuten `runImage`-Aufruf muss der vorhandene Container entfernt werden, sonst schlägt `docker run --name ccctest` wegen Namenskonflikt fehl.
