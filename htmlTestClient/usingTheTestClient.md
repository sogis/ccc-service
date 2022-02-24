# Verwendung des HTML-Testclient

## Verbindung öffnen

Zentral in den Schritten des Öffnens der Verbindung ist die UUID der Session.
Mit der von der Fachapplikation bestimmten UUID wird das Pairing zwischen 
Fachsystem und GIS für einen Benutzer bestimmt.

Historisch bedingt wird die UUID:
* in der URL **ohne** geschweifte Klammern angegeben.
* in der connectApp / connectGIS Nachricht **mit** geschweiften Klammern angegeben.

### Websocket-Verbindung öffnen

Die URL eingeben und die Websocket-Verbindung öffnen.

    wss://fuu/bar

### WebGIS Client aufrufen

In der Adressleiste des Browser den WGC aufrufen. Beispielsweise:

    https://geo.so.ch/map?appintegration=baugk&session=[UUID]

### Connect Nachricht schicken

Typischerweise in der Rolle der Fachapplikation:

    {
        "apiVersion": "1.0",
        "method": "connectApp",
        "session": "{[UUID]]}",
        "clientName": "Axioma Mandant AfU"
    }
    
## Payload Nachricht schicken

Beispielsweise, um ein Fachobjekt auf der Karte darzustellen

    {
        "method": "showGeoObject",
        "context": {
            "afu_geschaeft": 3671951
        },
        "data": {
            "type": "Point",
            "coordinates": [2609190, 1226652]
        }
    }

## Vorgehen für einen Funktionstest des CCC-Service

* Den HTML Test Client in zwei Browserfenstern öffnen;
  das eine wird die Fachanwendung simulieren ("App-Client"),
  das andere die GIS-Seite ("GIS-Client")
* In beiden Browserfenstern die Entwicklertools öffnen
  und sich den Netzwerkverkehr einblenden lassen
* Aus dem "App-Client" eine Verbindung herstellen,
  z.B. wss://geo-t.so.ch/ccc-service;
  in den Entwicklertools den Request markieren (anklicken)
* Aus dem "GIS-Client" die Verbindung zum gleichen Server herstellen;
  in den Entwicklertools ebenfalls den Request markieren
* Aus dem "App-Client" folgende Nachricht senden
  (wobei die UUID unter `session` beliebig sein kann;
  sie muss aber in beiden Requests gleich sein):
```
{
  "apiVersion": "1.0",
  "method": "connectApp",
  "session": "{E9C62508-025A-4A0F-B342-1B632282ABD8}",
  "clientName": "Test app"
}

```
  In den Entwicklertools sieht man nun beim markierten Request
  (in Firefox unter *Response*), dass die Nachricht gesendet worden ist
  (grüner Pfeil)
* Aus dem "GIS-Client" folgende Nachricht senden:
```
{
  "apiVersion": "1.0",
  "method": "connectGis",
  "session": "{E9C62508-025A-4A0F-B342-1B632282ABD8}",
  "clientName": "Test GIS"
}
```

In den Entwicklertools des "App-Clients" muss nun angezeigt werden,
dass ein `notifySessionReady` empfangen worden ist (roter Pfeil).
Auch in den Entwicklertools des "GIS-Clients" sieht man,
dass die `connectGis`-Nachricht gesendet worden ist,
und es muss ein `notifySessionReady` empfangen worden sein.
Dann ist die Verbindung der beiden Clients erfolgreich aufgebaut worden.
