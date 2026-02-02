> Automatisch generiert mit Claude Code

# CCC-Service Architektur

## Konzept-Uebersicht

Der CCC-Service (Client-Client Context) vermittelt bidirektionale Kommunikation zwischen einer **App** (Fachapplikation, z.B. BauGK) und einem **GIS** (Web GIS Client, z.B. SO!MAP) ueber WebSocket-Verbindungen. Die Fachapplikation hat die Datenhoheit -- das GIS speichert nie direkt, sondern meldet Aenderungen an die App zurueck.

```
  +-----------+                                      +-----------+
  |           |   WebSocket        WebSocket         |           |
  |    App    | <──────────>  CCC-Service  <──────── |    GIS    |
  |           |  /ccc-service               /ccc-service         |
  +-----------+                                      +-----------+
       |                                                  |
       |   connectApp ──────────>                         |
       |                         <──────── connectGis     |
       |   <── notifySessionReady ──>                     |
       |                                                  |
       |   editGeoObject ───────────────────────>         |
       |                  <───────── geoObjectSelected    |
       |                  <───── editGeoObjectDone        |
       |   notifyObjectUpdated ─────────────────>         |
       |   ...                                            |
```

## Schluesselbegriffe

### WebSocketSession (Spring)

Die rohe WebSocket-Verbindung von Spring. Jeder Client (App oder GIS) hat genau eine `WebSocketSession` zum Server. Der Server kennt die Session ueber ein internes Spring-Objekt.

### SockConnection

Wrapper um eine einzelne `WebSocketSession` mit Zusatzinformationen:

```
+-----------------------------------+
|         SockConnection            |
|-----------------------------------|
| clientName    "Axioma Mandant"    |
| apiVersion    "1.0" oder "1.2"   |
| key           CryptoKey           |  <-- fuer Reconnect (v1.2)
| webSocket     WebSocketSession    |  <-- die eigentliche Verbindung
+-----------------------------------+

Methoden:
  sendMessage(text)   Synchronisiertes Senden
  sendPing()          Keep-alive Ping
  keyEquals(key)      Reconnect-Key pruefen
  switchToNewWebSocketCon(ws)  Reconnect durchfuehren
```

### Session

Eine **Session** verbindet genau eine App-Verbindung mit einer GIS-Verbindung. Sie ist die zentrale Routing-Einheit:

```
+-------------------------------------------------------+
|                      Session                          |
|-------------------------------------------------------|
| sessionUid     UUID (vom Client vorgegeben)           |
| sessionNr      1, 2, 3, ... (menschenlesbar)         |
| appConnection  SockConnection ──> App WebSocket       |
| gisConnection  SockConnection ──> GIS WebSocket       |
| handshake      Zeitfenster 60 Sek.                    |
+-------------------------------------------------------+

Routing:
  App sendet Nachricht
    -> Session findet Peer (GIS)
    -> leitet weiter an GIS SockConnection
```

### Sessions (Registry)

Statische Klasse, die alle aktiven Sessions in einer `ConcurrentHashMap` verwaltet. Jede Session ist **zweimal** eingetragen (einmal mit App-WebSocket als Key, einmal mit GIS-WebSocket):

```
sessionsBySocket:
  +---------------------+------------------+
  | WebSocketSession    | Session          |
  +---------------------+------------------+
  | App-Socket (User1)  | --> Session #1   |
  | GIS-Socket (User1)  | --> Session #1   |
  | App-Socket (User2)  | --> Session #2   |
  | GIS-Socket (User2)  | --> Session #2   |
  +---------------------+------------------+

Lookup: O(1) ueber WebSocketSession
```

### CCCWebSocketHandler (Spring Einstiegspunkt)

Der zentrale Spring WebSocket-Handler, registriert auf `/ccc-service`:

```
Client verbindet sich
        |
        v
afterConnectionEstablished()
  |
  +-- Startet 2-Sekunden-Timer
  |   Falls kein Connect/Reconnect kommt -> Verbindung schliessen
  |
Client sendet Nachricht
        |
        v
handleTextMessage()
  |
  +-- MessageAccumulator.accumulate()
       |
       +-- (bei letztem Fragment) --> MessageHandler.handleMessage()
  |
Client trennt Verbindung
        |
        v
afterConnectionClosed()
  |
  +-- accumulator.cleanup()
```

### MessageHandler

Statische Hilfsklasse, die den JSON-String in ein typisiertes Message-Objekt umwandelt und verarbeitet:

```
handleMessage(webSocket, jsonString)
        |
        v
Message.forJsonString(json)          // Factory: JSON -> Message-Subklasse
        |
        v
message.process(webSocket)           // Polymorphe Verarbeitung
        |
        +-- Connect:    Session erstellen/vervollstaendigen
        +-- Reconnect:  WebSocket austauschen
        +-- Daten:      An Peer weiterleiten
        +-- Error:      An Peer weiterleiten
```

### MessageAccumulator

Behandelt fragmentierte WebSocket-Nachrichten (wenn eine Nachricht in mehreren Frames ankommt):

```
Frame 1 (partial): "{ \"method\": \"edit"
Frame 2 (partial): "GeoObject\", \"data"
Frame 3 (last):    "\": {} }"

  --> Zusammengebaut: "{ \"method\": \"editGeoObject\", \"data\": {} }"

Schutzmechanismen:
  - Timeout: 30 Sek. fuer unvollstaendige Nachrichten
  - Max. Groesse: 1 MB
  - Cleanup bei Connection-Close
```

## Nachrichten-Fluss im Detail

### Handshake (Session-Aufbau)

```
    App                    CCC-Service                    GIS
     |                         |                           |
     |--- connectApp --------->|                           |
     |    (sessionUid,         |                           |
     |     apiVersion)         |                           |
     |                         |<-------- connectGis ------|
     |                         |          (sessionUid,     |
     |                         |           apiVersion)     |
     |                         |                           |
     |<-- notifySessionReady --|-- notifySessionReady ---->|
     |    (sessionNr,          |   (sessionNr,             |
     |     connectionKey)      |    connectionKey)         |
     |                         |                           |
     |===== Session aktiv =====|==========================>|
```

### Daten-Nachrichten (Beispiel)

```
    App                    CCC-Service                    GIS
     |                         |                           |
     |--- editGeoObject ------>|--- editGeoObject -------->|
     |                         |                           |
     |                         |<-- geoObjectSelected -----|
     |<-- geoObjectSelected ---|                           |
     |                         |                           |
     |                         |<-- editGeoObjectDone -----|
     |<-- editGeoObjectDone ---|                           |
```

Daten-Nachrichten werden **1:1 weitergeleitet** (Raw-Message, keine Re-Serialisierung).

### Reconnect (nur v1.2)

```
    App                    CCC-Service                    GIS
     |                         |                           |
     | ~~~ Verbindung bricht ~~|                           |
     |                         |                           |
     |--- (neuer WebSocket) -->|                           |
     |--- reconnectApp ------->|                           |
     |    (oldConnectionKey)   |                           |
     |                         |                           |
     |    SockConnection.switchToNewWebSocketCon()         |
     |                         |                           |
     |===== Session laeuft weiter ========================>|
```

## Message-Typen

### App --> GIS

| Methode                | Klasse                 | Beschreibung                       |
|------------------------|------------------------|------------------------------------|
| `connectApp`           | `ConnectApp`           | Handshake initiieren               |
| `editGeoObject`        | `EditGeoObject`        | Bestehendes Objekt im GIS bearbeiten          |
| `createGeoObject`      | `CreateGeoObject`      | Neues Objekt im GIS erfassen                  |
| `showGeoObject`        | `ShowGeoObject`        | Objekt im GIS anzeigen (read-only)            |
| `cancelEditGeoObject`  | `CancelEditGeoObject`  | Laufende GIS-Aktion abbrechen                 |
| `changeLayerVisibility`| `ChangeLayerVisibility`| Layer im GIS ein-/ausblenden                  |
| `notifyObjectUpdated`  | `ObjectUpdated`        | Fachobjekt geaendert, GIS soll neu laden      |
| `reconnectApp`         | `ReconnectApp`         | Reconnect nach Verbindungsabbruch  |

### GIS --> App

| Methode                      | Klasse              | Beschreibung                        |
|------------------------------|----------------------|-------------------------------------|
| `connectGis`                 | `ConnectGis`         | Handshake initiieren                |
| `notifyGeoObjectSelected`    | `GeoObjectSelected`  | Objekt(e) auf Karte selektiert      |
| `notifyEditGeoObjectDone`    | `EditGeoObjectDone`  | GIS-Aktion abgeschlossen            |
| `reconnectGis`               | `ReconnectGis`       | Reconnect nach Verbindungsabbruch   |

### Server-generiert

| Methode              | Klasse          | Beschreibung                           |
|----------------------|-----------------|----------------------------------------|
| `notifySessionReady` | `SessionReady`  | Handshake abgeschlossen                |
| `notifyError`        | `Error`         | Fehlermeldung an Client                |
| `keyChange`          | `KeyChange`     | Neuer Reconnect-Key (alle 5 Min.)     |

## Hintergrund-Daemons

```
+------------------+----------------+------------------------------------------+
| Daemon           | Intervall      | Aktion                                   |
+------------------+----------------+------------------------------------------+
| SessionsKiller   | taeglich 3:00  | Alle Sessions schliessen, Zaehler reset  |
| SessionsGroomer  | alle 5 Min.    | Stale Sessions + abgelaufene Buffer      |
|                  |                | entfernen                                |
| PingSender       | alle 5 Min.    | WebSocket-PING an alle offenen Sessions  |
| KeyChanger       | alle 5 Min.    | Neue Keys an v1.2-Verbindungen senden    |
+------------------+----------------+------------------------------------------+

Zeitlicher Ablauf (Beispieltag):
  00:00 ─── Groomer, Ping, KeyChanger laufen alle 5 Min. ───
  03:00 ─── SessionsKiller: alle Sessions gekillt, Zaehler -> 0
  03:01 ─── Erste neue Session bekommt Nr. 1
  ...
  23:55 ─── Letzte Sessions des Tages
```

## CryptoKey und Reconnect-Sicherheit

```
SockConnection
  |
  +-- CryptoKey
        |
        +-- Aktueller Key:  "U2FsdGVkX1+abc..."
        +-- Grace Period:    60 Sekunden
        +-- Alter Key:       "U2FsdGVkX1+xyz..."  (noch gueltig)
        |
        +-- keyEquals("U2FsdGVkX1+abc...")  -> true  (aktuell)
        +-- keyEquals("U2FsdGVkX1+xyz...")  -> true  (Grace Period)
        +-- keyEquals("falscherKey")        -> false

KeyChanger (alle 5 Min.):
  1. refreshKey() -> neuer Key generiert
  2. keyChange-Nachricht an Client mit neuem Key
  3. Alter Key bleibt 60 Sek. gueltig (Grace Period)
```

## Gesamtarchitektur

```
+------+                                                            +------+
|      |  WebSocket /ccc-service                 WebSocket          |      |
| App  | <---------------------------+-------------------------->  | GIS  |
|      |                             |                              |      |
+------+                             |                              +------+
                                     |
              +----------------------|-------------------------------+
              |                CCC-Service (Spring Boot)             |
              |                      |                               |
              |            +---------v----------+                    |
              |            | CCCWebSocketHandler |                   |
              |            +---------+----------+                    |
              |                      |                               |
              |            +---------v----------+                    |
              |            | MessageAccumulator  |                   |
              |            +---------+----------+                    |
              |                      |                               |
              |            +---------v----------+                    |
              |            |   MessageHandler    |                   |
              |            +---------+----------+                    |
              |                      |                               |
              |            +---------v----------+                    |
              |            | Message.forJsonString()                 |
              |            | message.process()  |                    |
              |            +---------+----------+                    |
              |                      |                               |
              |            +---------v----------+                    |
              |            |     Sessions       |<----+              |
              |            |    (Registry)      |     |              |
              |            +---------+----------+     |              |
              |                      |                |              |
              |            +---------v----------+     |              |
              |            |      Session       |     |              |
              |            |                    |     |              |
              |            | +----------------+ |     | Daemons:     |
              |  App <------>| SockConnection | |     |              |
              |  WebSocket | +----------------+ |  +--+-----------+  |
              |            |                    |  | SessionsKiller|  |
              |            | +----------------+ |  | (3:00 daily)  |  |
              |  GIS <------>| SockConnection | |  +--------------+  |
              |  WebSocket | +----------------+ |  | Sess.Groomer |  |
              |            +--------------------+  | (alle 5 Min.)|  |
              |                                    +--------------+  |
              |                                    | PingSender   |  |
              |                                    | (alle 5 Min.)|  |
              |                                    +--------------+  |
              |                                    | KeyChanger   |  |
              |                                    | (alle 5 Min.)|  |
              |                                    +--------------+  |
              +------------------------------------------------------+

Nachrichtenfluss (Beispiel: App sendet editGeoObject):

  App ──WebSocket──> CCCWebSocketHandler
                       > MessageAccumulator
                       > MessageHandler
                       > EditGeoObject.process()
                           > Sessions.findByConnection(appSocket)
                           > session.getPeerConnection(appSocket) --> gisSockConnection
                           > gisSockConnection.sendMessage(rawMessage)
                                                      > GIS WebSocket ──> GIS
```

## Thread-Safety

| Klasse/Methode                        | Mechanismus              |
|---------------------------------------|--------------------------|
| `Sessions.sessionsBySocket`           | `ConcurrentHashMap`      |
| `Connect.addClient()`                 | `synchronized` Block     |
| `Sessions.resetSessionCollection()`   | `synchronized` Block     |
| `SockConnection.sendMessage()`        | `synchronized` Methode   |
| `SockConnection.sendPing()`           | `synchronized` Methode   |
| `CryptoKey.refreshKey/isEqual/getKey` | `synchronized` Methoden  |
