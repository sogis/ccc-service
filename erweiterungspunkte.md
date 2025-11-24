# Weiterfahren

# Codeänderungen für die SEIN-Ereiterungen

## Verantwortung und Beziehung der grundlegenden Klassen

### Message

Verarbeitet einkommende Nachrichten durch:
* Weiterleitung an Gegenüber (App oder GIS)
* Aktualisierung des State der Route
* Benachrichtigung von App und GIS bei Fehlern

Nutzt in der Verarbeitung die RouteCollection für die Aktualisierung des State

### Route

Verknüpft die beiden Connections Server - GIS und Server - App zu einer bidirektionalen Route
GIS - Server - App.

### RouteCollection

Umfasst alle aktiven Routen und bietet Hilfsfunktionen, um

### SocketHandler (Erweiterung der Spring-Klasse TextWebSocketHandler)

Erstellt für jede ankommende Nachricht eine Message und ruft deren process() Methode auf.

### Beispiel-Ablauf für die geroutete Nachricht "EditGeoObject"

* SocketHandler.handleMessage()
* EditGeoObject.process()
    * Finden der Route mittels der übergebenen Spring WebSocketSession der App-Connection
    * Weiterleiten der Nachricht auf die WebSocketSession der GIS-Connection



## changeLayerVisibility

## reconnect und keyChange

* SessionId erweitern.
  SessionId ist aktuell die Session. Erweitern auf Session mit den erforderlichen Properties
* SessionPool: Versuchen zu verstehen, wieso die Connections, die SessionId und der SessionState nicht Teil der Session-Klasse sind. Dazu prüfen, wo und für was die verschiedenen Hashes verwendet werden.
* BackgroundService: Verstehen, ob es OK ist, im BackgroudService den KeyChange zu implementieren. Risiko RaceCondition und Multithreading.

## Aufbau einer Session

* Session_UID
* Session_Nr
* GisChannel
* AppChannel
* Channel
    * ClientName
    * Connected since
    * Protocolversion
    * ChannelSession -> Spring
    * Channel_UID


