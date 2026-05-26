# CCC-Service konfigurieren

Alle Parameter können beim Start des Docker-Containers als Umgebungsvariablen übergeben werden. Spring Boot übersetzt dabei automatisch: Punkte → Unterstriche, alles Grossbuchstaben.

```
docker run -e PARAMETER=WERT sogis/ccc-service
```

---

## WebSocket

| Property | Env-Variable | Standard | Beschreibung |
|---|---|---|---|
| `ccc.websocket.connect-msg-max-delay-seconds` | `CCC_WEBSOCKET_CONNECT_MSG_MAX_DELAY_SECONDS` | `2` | Maximale Zeit in Sekunden, die eine neue WebSocket-Verbindung warten darf, bis die erste Connect-Nachricht eintrifft. Läuft die Frist ab, schliesst der Server die Verbindung. |
| `ccc.websocket.max-text-message-buffer-size` | `CCC_WEBSOCKET_MAX_TEXT_MESSAGE_BUFFER_SIZE` | `102400` | Maximale Grösse eines eingehenden Text-Frames in Bytes (100 KB). Grössere Nachrichten werden über mehrere Frames akkumuliert (siehe Accumulator). |
| `ccc.websocket.max-binary-message-buffer-size` | `CCC_WEBSOCKET_MAX_BINARY_MESSAGE_BUFFER_SIZE` | `8192` | Maximale Grösse eines eingehenden Binary-Frames in Bytes. |
| `ccc.websocket.idle-check-period-seconds` | `CCC_WEBSOCKET_IDLE_CHECK_PERIOD_SECONDS` | `1` | Wie oft (Sekunden) der Tomcat-Hintergrundthread ablaufende Idle-Timeouts prüft. Kleinere Werte erhöhen die Genauigkeit des `connect-msg-max-delay`, verursachen aber minimalen CPU-Overhead. |

---

## Message-Accumulator

Fragmentierte WebSocket-Frames werden serverseitig zu vollständigen Nachrichten zusammengesetzt.

| Property | Env-Variable | Standard | Beschreibung |
|---|---|---|---|
| `ccc.accumulator.timeout-seconds` | `CCC_ACCUMULATOR_TIMEOUT_SECONDS` | `30` | Maximale Zeit in Sekunden, die ein unvollständiges Fragment-Set im Puffer verweilen darf, bevor es verworfen wird. |
| `ccc.accumulator.max-size` | `CCC_ACCUMULATOR_MAX_SIZE` | `1048576` | Maximale Gesamtgrösse einer akkumulierten Nachricht in Bytes (1 MB). |

---

## Security

| Property | Env-Variable | Standard | Beschreibung |
|---|---|---|---|
| `ccc.security.max-sessions` | `CCC_SECURITY_MAX_SESSIONS` | `200` | Maximale Anzahl gleichzeitiger CCC-Sessions. Neue Sessions werden mit Status 1013 (Service Overload) abgewiesen, wenn das Limit erreicht ist. Reconnects und der zweite Client einer bestehenden Session sind nicht betroffen. `0` = unbegrenzt. |
| `ccc.security.connection-limiter.enabled` | `CCC_SECURITY_CONNECTION_LIMITER_ENABLED` | `false` | Aktiviert das IP-basierte Connection-Limit (max. 10 gleichzeitige / 30 neue Verbindungen pro Minute und IP). Normalerweise übernimmt der vorgelagerte SES diese Aufgabe. |
| `ccc.security.rate-limiter.enabled` | `CCC_SECURITY_RATE_LIMITER_ENABLED` | `false` | Aktiviert das IP-basierte Rate-Limiting mit exponentiellem Backoff bei fehlgeschlagenen Connect/Reconnect-Versuchen. Normalerweise übernimmt der vorgelagerte SES diese Aufgabe. |

---

## Tomcat / Verbindungslimits

Schützt den Service vor Verbindungsstürmen, falls der vorgelagerte Rate-Limiter ausfällt oder umgangen wird.

| Property | Env-Variable | Standard | Beschreibung |
|---|---|---|---|
| `server.tomcat.max-connections` | `SERVER_TOMCAT_MAX_CONNECTIONS` | `400` | Maximale Anzahl gleichzeitig akzeptierter Verbindungen (HTTP + WebSocket). |
| `server.tomcat.threads.max` | `SERVER_TOMCAT_THREADS_MAX` | `50` | Maximale Anzahl Worker-Threads. Der CCC-Service ist I/O-gebunden; 50 Threads sind für den typischen Betrieb ausreichend. |
| `server.tomcat.accept-count` | `SERVER_TOMCAT_ACCEPT_COUNT` | `20` | Grösse des OS-Socket-Backlogs, wenn alle Worker-Threads ausgelastet sind. |
| `server.tomcat.connection-timeout` | `SERVER_TOMCAT_CONNECTION_TIMEOUT` | `10s` | Zeit, die der Server auf den HTTP-Request (WebSocket-Upgrade-Handshake) wartet, bevor die Verbindung abgewiesen wird. |

---

## Graceful Shutdown

| Property | Env-Variable | Standard | Beschreibung |
|---|---|---|---|
| `spring.lifecycle.timeout-per-shutdown-phase` | `SPRING_LIFECYCLE_TIMEOUT_PER_SHUTDOWN_PHASE` | `20s` | Zeit, die dem Service beim Herunterfahren (SIGTERM) bleibt, um bestehende WebSocket-Verbindungen sauber zu schliessen. Relevant für Kubernetes Rolling Updates. |

---

## Debug-Logging

| Env-Variable | Wert | Beschreibung |
|---|---|---|
| `CCC_DEBUG` | beliebiger nicht-leerer Wert | Aktiviert das Debug-Profil (`application-ccc-debug.properties`): erhöht den Log-Level für `ch.so.agi.cccservice` auf `DEBUG`. |

```
# Debug aktivieren
docker run -e CCC_DEBUG=1 sogis/ccc-service

# Debug explizit deaktivieren (z.B. Override einer Basis-Konfiguration)
docker run -e CCC_DEBUG= sogis/ccc-service
```
