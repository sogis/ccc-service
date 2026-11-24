<!-- WebSocket-Vermittlungsdienst zwischen GIS-Kartenapplikationen und Fachapplikationen (SOGIS CCC-Protokoll). -->

# CCC-Service

Der CCC-Service verbindet als "Mittelsmann" GIS-unwillige Fachapplikationen mit den
Kartenapplikationen des AGI über bidirektionale WebSocket-Sessions. Details zum
Protokoll: siehe [Projekt-README](https://github.com/sogis/ccc-service).

## Verwendung

```
docker pull sogis/ccc-service
docker run -d -p 8080:8080 --name ccctest sogis/ccc-service
```

Der Dienst ist danach unter `ws://localhost:8080/ccc-service` erreichbar,
Health-Check unter `http://localhost:8080/actuator/health`. Weitere Details:
[docs/user/running.md](https://github.com/sogis/ccc-service/blob/master/docs/user/running.md).

## Sicherheitsbewertung (VEX)

Scanner (z. B. Grype, Trivy, Docker Scout) melden für dieses Image CVEs, die entweder
bereits behoben sind oder deren verwundbarer Codepfad im Service nicht erreichbar ist.
Die Bewertung jeder gemeldeten CVE (Status `fixed` / `not_affected` mit Begründung)
wird als [OpenVEX](https://openvex.dev) gepflegt:

- VEX-Dokument: [docs/security/ccc-service.openvex.json](https://github.com/sogis/ccc-service/blob/master/docs/security/ccc-service.openvex.json)
- Generator: [docs/security/generate_vex.py](https://github.com/sogis/ccc-service/blob/master/docs/security/generate_vex.py)
  (Ablauf zur Aktualisierung nach einem Image-Neubau steht im Kopf des Skripts)

Das VEX ist an den Digest eines bestimmten Images gebunden. Scan mit Berücksichtigung
des VEX:

```
grype --vex ccc-service.openvex.json sogis/ccc-service:<tag>
```
