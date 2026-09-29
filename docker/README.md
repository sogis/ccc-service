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

## Bekannte CVEs, die diesen Service nicht betreffen

Scanner (z. B. Docker Scout, Trivy) melden für dieses Image gelegentlich CVEs, deren
verwundbarer Codepfad hier nicht erreichbar ist. Diese Liste dokumentiert den
jeweiligen Grund, damit er bei jedem Scan nicht neu recherchiert werden muss. Stand:
2026-09-21, geprüft gegen Quellcode und Dockerfile in diesem Repo.

### Alpine-Basisimage (`eclipse-temurin:25-jre-alpine`)

Diese CVEs betreffen transitive OS-Pakete des Basisimages, die vom Service nicht
genutzt werden. Der Service ist ein reiner JSON-Nachrichten-Router ohne Bild-,
Font- oder Shell-Verarbeitung zur Laufzeit.

| CVE | Paket | CVSS | Begründung |
|---|---|---|---|
| CVE-2025-60876 | busybox 1.37.0-r30 | 6.5 | Läuft als PID 1 nur der `java`-Prozess (via `exec`), keine sonstige busybox-Applet-Nutzung zur Laufzeit. **Einschränkung:** Das `CMD` ist Shell-Form (`exec java ...`), busybox' `ash` parst die Zeile kurz beim Start, bevor sie sich per `exec` selbst ersetzt. Betrifft die CVE das `ash`-Applet selbst (statt z. B. `wget`/`tar`/`unzip`), gilt die Begründung nicht unbesehen — im ursprünglichen Dockerfile-Eintrag nicht differenziert, bei der nächsten Bewertung prüfen. |
| CVE-2026-25646 / CVE-2026-40930 | libpng 1.6.57-r0 | 5.4 | Keine Bildverarbeitung im Service; libpng wird nie geladen. |
| CVE-2026-23865 | freetype 2.14.1-r0 | 5.3 | Kein Font-Rendering im Service; freetype wird nie geladen. |
| CVE-2026-34182 | openssl | 9.1 | Betrifft CMS/S-MIME (`AuthEnvelopedData`); die JVM nutzt für TLS ihren eigenen Stack (JSSE), kein System-OpenSSL. |
| CVE-2016-2781 | coreutils 9.8-r1 | 4.6 | chroot-bezogen; Container läuft als Non-Root (uid 1001), `chroot` wird von der Anwendung nie aufgerufen. |

### Applikations-Abhängigkeiten (nicht erreichbarer Codepfad, trotzdem proaktiv gepatcht)

Diese CVEs sind in `build.gradle` per Versions-Override bereits gefixt (defense in
depth), obwohl der verwundbare Codepfad laut Analyse nicht erreichbar ist:

| CVE | Bibliothek | CVSS | Begründung |
|---|---|---|---|
| CVE-2026-59889 | Jackson (`@JsonView`-Bypass) | 6.5 | Verifiziert: keine `@JsonView`- oder `@JsonTypeInfo`-Nutzung im Quellcode (`src/`). |
| CVE-2026-49844 | Log4j API (`MapMessage.asJson()`) | 6.3 | Verifiziert: keine `MapMessage`- oder Log4j2-API-Nutzung; Logging läuft ausschliesslich über SLF4J/Logback, Log4j-API ist nur transitiv (Bridge) vorhanden. |
| CVE-2026-41850 | Spring Framework (SpEL algorithmic DoS) | 7.5 | Verifiziert: keine Auswertung von benutzerseitig eingegebenem SpEL im Code. |
| CVE-2026-41846 | Spring Framework (XSS via JSP-Formular-Tags) | 5.9 | Verifiziert: keine JSP/Thymeleaf-Abhängigkeit, keine View-Schicht — der Service liefert keine HTML-Views aus. |
| CVE-2026-41845 | Spring Framework (XSS via `JavaScriptUtils.javaScriptEscape()`) | 7.1 | Neu identifiziert (nicht im ursprünglichen `build.gradle`-Kommentar erwähnt): Verifiziert per Grep — keine `JavaScriptUtils`-Nutzung, keine HTML/JS-Ausgabe durch den Service. |

**Nicht in dieser Liste**, weil der zugehörige Kommentar in `build.gradle` den
Codepfad explizit als *potenziell erreichbar* einstuft:
- Tomcat-Auth-Bypässe (CVE-2026-65905, CVE-2026-65182, CVE-2026-68525 u. a.) — Tomcat ist der aktiv genutzte eingebettete Servlet-Container.
- Spring-Framework Static-Resource-CVEs (CVE-2026-41842, CVE-2026-41841) — `spring-boot-starter-web` konfiguriert die Resource-Handler-Pipeline automatisch.
- Micrometer-CVEs (CVE-2026-40984, CVE-2026-40983) — `spring-boot-starter-actuator` instrumentiert jeden HTTP-Request.

## Pflege dieser Liste

Diese Datei fasst die CVE-Begründungen aus [`docker/Dockerfile`](Dockerfile) und
[`build.gradle`](../build.gradle) zusammen. Bei neuen Scanner-Meldungen: Grund hier
und an der jeweiligen Quelle dokumentieren. Diese Liste basiert auf manueller
Code-Analyse, nicht auf einem automatisierten Scan (kein Trivy/Grype in der CI
konfiguriert) — bei Bedarf ergänzen.
