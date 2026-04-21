# Entwicklerdokumentation

## Verantwortung und Beziehung der grundlegenden Klassen

### Message (Package ch.so.agi.cccservice.message)

Die Kindklassen von Message verarbeiten einkommende Nachrichten durch:
* Weiterleitung an Gegenüber (App oder GIS)
* Aktualisierung des State der Route (Insbesondere während Handshake)
* Benachrichtigung von App und GIS bei Fehlern

Nutzt in der Verarbeitung die Session-Collection "Sessions" um die für die eingehende
Nachricht zutreffende Session zu finden.

### Session (Package ch.so.agi.cccservice.session)

Verknüpft die beiden Connections Server - GIS und Server - App zu einer bidirektionalen Route
GIS - Server - App.

### Sessions - aka Session-Collection (Package ch.so.agi.cccservice.session)

Umfasst alle geöffneten Sessions. Die Messages verwenden die Session-Collection,
um für die Verarbeitung einer Nachricht die zutreffende Session zu finden.

### MessageHandler (Package ch.so.agi.cccservice)

Erstellt für jede ankommende Nachricht eine Message und ruft deren process() Methode auf.

## Deamons (Package ch.so.agi.cccservice.deamon)

Im Package sind die Klassen von Hintergrund-Diensten enthalten, welche mittels Spring
"sceduled" werden.

## Monitoring

Zwecks betrieblichem Monitoring sind die folgenden Klassen implementiert:
* StatusPage (Package ch.so.agi.cccservice.http):
  * Gibt auf dem Root-Pfad als Antwort auf ein HTTP-Get die Version des CCC-Service und eine Aufschlüsselung der Sessions nach Zustand aus (offen, wartend auf Reconnect, unvollständiger Handshake).
  * Default-Pfad: http://localhost:8080
* WebSocketHealthIndicator (Package ch.so.agi.cccservice.health):
  * Prüft die Gesundheit des Service durch Verschicken von Testnachrichten von einem Test-Client (Reconnect + Payload).
  * Default-Pfad: http://localhost:8080/actuator/health/liveness
* ReadinessProbe (Package ch.so.agi.cccservice.health):
  * Leichtgewichtiger Readiness-Check für Kubernetes Rolling Updates. Gibt UP zurück, sobald Spring Boot bereit ist.
  * Default-Pfad: http://localhost:8080/actuator/health/readiness

## Konzept für die automatischen Tests

Damit in den meisten Fällen leichtgewichtige und damit sehr schnelle automatische Tests ausgeführt
werden können, ist der grösste Teil der Logik des CCC-Service nur abhängig vom Interface "WebSocketSession".

Das Interface repräsentiert eine WebSocket-Verbindung. Für alle leichtgewichtigen Tests implementiert
die Mockup-Klasse "MockWebSocketSession" das Interface. Die Tests mit "MockWebSocketSession" laufen damit
komplett von Spring isoliert.

Wenige Tests gehen über den Scope von MockWebSocketSession hinaus. Für diese wird vor der Testausführung
ein Spring-Kontext hochgefahren.

### Smoke Tests

Smoke Tests prüfen einen bereits deployten CCC-Service. Sie sind mit dem JUnit-Tag `smoke` annotiert
und werden mit dem Gradle-Task `smokeTest` ausgeführt:

```bash
./gradlew smokeTest -Dccc.smoke.url=wss://<host>/ccc-service
```

Die System-Property `ccc.smoke.url` gibt die Basis-URL des zu testenden Service an.
Ohne diese Property wird der Wert aus `application.properties` verwendet.

## Versionierung

### Quellen

Die Version setzt sich aus zwei Quellen zusammen:

* `cccVersion` in `gradle.properties` — die aktuell gepflegte Basisversion im Format `MAJOR.MINOR.PATCH` (z.B. `1.2.2`).
* Umgebungsvariablen `RELEASE_TAG` bzw. `GITHUB_RUN_NUMBER` — werden vom GitHub-Actions-Workflow gesetzt.

### Ermittlung des Versions-Strings

Die Funktion `buildIdent()` in `build.gradle` bestimmt die Version nach folgender Regel:

| Kontext | `RELEASE_TAG` | `GITHUB_RUN_NUMBER` | Ergebnis |
|---|---|---|---|
| Lokaler Build | — | — | `<cccVersion>.localbuild` (z.B. `1.2.2.localbuild`) |
| CI-Build auf `master` | — | gesetzt | `<cccVersion>.<run>` (z.B. `1.2.2.457`) |
| Release (GitHub Release published) | gesetzt | gesetzt | `<RELEASE_TAG>` (z.B. `1.2.2` oder `1.2.2b3`) |

### Drift-Schutz zwischen `cccVersion` und `RELEASE_TAG`

Damit `cccVersion` und GitHub-Release-Tag nicht auseinanderlaufen, validiert `buildIdent()` den Tag gegen die Basisversion. Erlaubt sind:

* Exakter Match: Tag gleich `cccVersion` (z.B. `cccVersion=1.2.2` → Tag `1.2.2`).
* Beta-Suffix: Tag gleich `cccVersion` + `b` + Zahl (z.B. `1.2.2b3`).

Jeder andere Tag (z.B. `1.2.3`, `9.9.9`, `1.2.2b`) bricht den Build mit einer Fehlermeldung ab. Konsequenz für den Release-Workflow:

* **Patch-Release** (`1.2.2` → `1.2.3`): zuerst `cccVersion=1.2.3` in `gradle.properties` committen, dann Tag `1.2.3` erstellen.
* **Beta-Release** auf bestehender Basis: direkt Tag `1.2.2bN` erstellen, `gradle.properties` bleibt unverändert.

### Weg der Version in den Service

1. `build.gradle` setzt `version = buildIdent()` auf das Gradle-Projekt.
2. Der Spring-Boot-Task `bootBuildInfo` erzeugt daraus `META-INF/build-info.properties` mit `build.version=<version>`.
3. Spring Boot Actuator stellt daraus automatisch eine `BuildProperties`-Bean bereit.
4. `StatusPage` (Package `ch.so.agi.cccservice.http`) injiziert die Bean und zeigt `build.version` auf `GET /` an.

Der gebaute Jar wird von `bootJar` bewusst auf den festen Namen `ccc-service.jar` gesetzt (nicht `ccc-service-<version>.jar`), damit das Dockerfile sowie der `jardist`-Task unverändert bleiben. Die Version steckt nur innerhalb des Jars in `build-info.properties` und im Docker-Image-Tag (siehe `buildImage` in `build.gradle`).

