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

- VEX-Dokument: [docs/security/ccc-service.openvex.json](https://github.com/sogis/ccc-service/blob/master/docs/security/ccc-service.openvex.json),
  zusätzlich im Image unter `/home/ccc/ccc-service.openvex.json` (Label `ccc-service.vex`)
- Generator: [docs/security/generate_vex.py](https://github.com/sogis/ccc-service/blob/master/docs/security/generate_vex.py)
  (Ablauf bei neuen Scanner-Meldungen steht im Kopf des Skripts)

Das VEX gilt für alle Images von `sogis/ccc-service`; es muss nur bei neu gemeldeten,
noch nicht bewerteten CVEs ergänzt werden. Scanner lesen es nicht selbst aus dem Image,
es wird per `--vex` übergeben:

```
docker run --rm --entrypoint cat sogis/ccc-service:<tag> /home/ccc/ccc-service.openvex.json > ccc-service.openvex.json
grype --vex ccc-service.openvex.json sogis/ccc-service:<tag>
# oder
trivy image --vex ccc-service.openvex.json --show-suppressed sogis/ccc-service:<tag>
```

Variante mit Trivy in Docker:
```
docker run --rm --entrypoint cat sogis/ccc-service:latest /home/ccc/ccc-service.openvex.json > ccc-service.openvex.json

docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v trivy-db:/root/.cache/ \
    -v "$PWD:/vex:ro" \
    --cpus=4 \
    aquasec/trivy:latest image \
    --vex /vex/ccc-service.openvex.json \
    --show-suppressed \
    sogis/ccc-service:latest

```

Trivy wendet das VEX nur auf Images an, die bereits in der Registry liegen (Registry-Digest
vorhanden); Grype auch auf lokal gebaute Images.

Beim Release prüft der CI-Workflow mit Docker Scout und bricht bei nicht bewerteten
HIGH/CRITICAL-CVEs ab. Docker Scout erkennt die generische Produkt-ID nicht; es braucht eine
Variante mit `pkg:docker/sogis/ccc-service@<tag>` als Produkt (ohne Subcomponents), die der
Generator mit `--product` erzeugt:

```
python3 docs/security/generate_vex.py --version 1 --product pkg:docker/sogis/ccc-service@<tag> --output /tmp/vex/ccc-service.vex.json
docker scout cves --only-severity critical,high --vex-author sogis --vex-location /tmp/vex sogis/ccc-service:<tag>
```

- Ohne `--vex-author sogis` ignoriert Docker Scout das VEX (Standard: nur Autoren `<.*@docker.com>`).
- Das VEX wird nicht als Docker-Scout-Attestation angehängt: `docker scout cves` (v1.30.0)
  wendet VEX aus Attestationen nicht an (getestet), nur per `--vex-location`.
