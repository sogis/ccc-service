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

