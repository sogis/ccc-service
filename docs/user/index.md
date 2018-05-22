# ccc-service

Der ccc-service dient dazu, eine Kommunikation 
zwischen GIS und Fachapplikation zu ermöglichen.

Der «Smoketest» um im Betrieb die Verfügbarkeit des ccc-services zu testen, ist [hier]((probetool.md) beschrieben.

## System Anforderungen
Um die aktuelle Version vom ccc-service auszuführen, muss 
 - docker, Version 18.0 oder neuer, installiert sein.

Die docker-Software kann auf der Website http://www.docker.com/ gratis bezogen werden.

## Ausführen
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
    
Der ccc-service unterstütz zusätzlich die folgenden Umgebungsvariablen:

Variable           | Beschreibung
-------------------|----------------
CCC_MAX_INACTIVITY | Maximal zulässige Zeit ohne Meldungsaustausch in Sekunden (Default: 2h)
CCC_MAX_PAIRING    | Maximal zulässige Zeit zwischen GIS- und Fachapplikations-Verbindungsaufbau in Sekunden (Default: 60s)

