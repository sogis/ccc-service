# ccc-service

Der ccc-service dient dazu, eine Kommunikation 
zwischen GIS und Fachapplikation zu ermöglichen.

Der «Smoketest» um im Betrieb die Verfügbarkeit des ccc-services zu testen, ist [hier](probetool.md) beschrieben.

## System Anforderungen
Um die aktuelle Version vom ccc-service auszuführen, muss 
 - docker, Version 18.0 oder neuer, installiert sein.

Die docker-Software kann auf der Website http://www.docker.com/ gratis bezogen werden.

## Bezug des Image
Das Image ist im Docker Hub unter https://hub.docker.com/r/sogis/ccc-service/ publiziert. Es wird bezogen mittels docker pull Befehl:

    docker pull sogis/ccc-service

## Starten
Um den ccc-service auszuführen, geben Sie auf der Kommandozeile folgendes Kommando ein:

    docker run -d -p 8081:8080 --name ccctest sogis/ccc-service

Der ccc-service ist nun unter der URL ws://localhost:8081/ccc-service ansprechbar.
    
Um die Logs des ccc-service anzusehen, geben Sie auf der Kommandozeile folgendes Kommando ein:

    docker logs ccctest
    
Um den ccc-service zu beenden, geben Sie auf der Kommandozeile folgende Kommandos ein:

    docker kill ccctest
    docker rm ccctest

Um den ccc-service mit ausführlicherem Log auszuführen, geben Sie auf der Kommandozeile folgendes Kommando ein:

    docker run -d -p 8080:8080 -e CCC_DEBUG=1 --name ccctest sogis/ccc-service
    
Der ccc-service unterstützt zusätzlich die folgenden Umgebungsvariablen:

Variable           | Beschreibung
-------------------|----------------
CCC_MAX_INACTIVITY | Maximal zulässige Zeit ohne Meldungsaustausch in Sekunden (Default: 7200s --> 2h)
CCC_MAX_PAIRING    | Maximal zulässige Zeit zwischen GIS- und Fachapplikations-Verbindungsaufbau in Sekunden (Default: 60s)
CCC_PING_INTERVAL  | Nach Ablauf dieser Zeit (in Sekunden; Default: 300s) ohne Meldungsaustausch, sendet der Service eine Ping-Meldung an GIS und Fachapplikation
CCC_DEBUG          | CCC_DEBUG=1 aktiviert das Schreiben von umfangreichen Logs zwecks debugging.

## Prüfen (Health-Check)

Der standart Health-Check von Spring Boot ist aktiviert und unter 

    http://localhost:8081/actuator/health
    
erreichbar.

## HTML-Client
Im Verzeichnis ccc-service/htmlTestClient/ ist die Datei htmlTestClient.html enthalten. Diese stellt im Browser ein einfaches GUI dar, mit welchem Nachrichten mit einem Websocket Server ausgetauscht werden können. Durch Verbinden auf die Adresse des CCC-Service kann der Client als "Mock" sowohl für das GIS wie auch die Fachapplikation verwendet werden.



