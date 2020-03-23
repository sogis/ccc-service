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

