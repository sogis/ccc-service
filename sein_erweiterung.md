# Erweiterung des CCC-Protokolls für SEIN

## Überblick der neuen Nachrichten

|Name|Richtung|Typ|Beschreibung|
|---|---|---|---|
|changeLayerVisibility|F > K|RUN|Aufforderung an die Kartenapplikation, eine geladene Ebene auf sichtbar/unsichtbar zu schalten.|
|reconnectGis & reconnectApp|F,K > CCC|RUN|Anfrage eines V1.2-Clients, nach einem Verbindungsunterbruch wieder in die bestehende Session aufgenommen zu werden.|
|keyChange|CCC > F,K|RUN|Aufforderung des Servers an einen V1.2-Client, die Keys auszutauschen.|

## Erweiterte Nachrichten aufgrund der Reconnects

|Name|Richtung|Typ|
|---|---|---|
|notifySessionReady|CCC > F,K|INIT|

## changeLayerVisibility

### Aufbau der Nachricht

```json
{
    "method": "changeLayerVisibility",
    "data": {
        "layerIdentifier": "ch.so.afu.abbaustellen",
        "visible": true
    }
}
```
### Antworten vom GIS

Vom GIS wird keine Antwort zurückgeschickt, sofern der Befehl ausgeführt werden konnte.   
Dies gilt für die Fälle:
* Die Sichtbarkeit der Ebene wurde wie angefordert umgeschaltet.
* Die angeforderte Sichtbarkeit liegt bereits vor. Der Einfachheit halber wird dies nicht als Fehler behandelt.
* Die auf sichtbar zu schaltende Ebene ist noch nicht geladen: Die Ebene wird zuoberst im Ebenenstapel dazugeladen und auf sichtbar gestellt.
* Die auf unsichtbar zu schaltende Ebene ist noch nicht geladen: Die Ebene wird zuoberst im Ebenenstapel dazugeladen und auf unsichtbar gestellt.

NotifyError-Antwort, falls der Layer nicht bekannt ist:

```json
{
    "method": "notifyError",
    "code": 404,
    "message": "Can not set the layer visibility. Layer [layerIdentifier] is unknown",
    "userData": null,
    "nativeCode": "Err-165",
    "technicalDetails": "java.lang.IllegalArgumentException:
    at example.common.TestTry.execute(TestTry.java:17)
    at example.common.TestTry.main(TestTry.java:11)"
}
```

Bemerkungen: 
* [layerIdentifier] wird dynamisch mit dem effektiven Layeridentifier ersetzt (Beispielsweise "ch.so.afu.abbaustellen")
* Das CCC-Protokoll verwendet explizites Null-Handling. Die Keys userData, nativeCode und technicalDetails sind also immer vorhanden. Ihre Werte können null sein.
* Die Keys nativeCode und technicalDetails sollten vom Web GIS Client mit sinnvollem Inhalt versehen werden.
* Dieser Error wird auch ausgelöst, falls der angeforderte und der effektive Identifier nicht übereinstimmen.

## Reconnects

### Motivation

Die bei der Ersteinführung versprochene Netzstabilität über einen Zeitraum von 4 Stunden hat sich als Phasenweise nicht erfüllt erwiesen. Zudem wechseln die nun eingesetzten mobilen Geräte sowieso häufig ihre Netzwerkverbindung (Dock auf WLAN etc.).

Damit der Benutzer von den Unterbrüchen nichts merkt, wird das Protokoll um die Nachrichten reconnect und keyChange erweitert.

### Sicherheit

Alle auf Protokoll 1.2 arbeitenden Clients müssen neben den reconnect-Nachrichten auch die keyChange-Aufforderung des CCC-Servers implementieren, damit ein aus irgendeinem Grund in die falschen Hände geratener Key nur für eine kurze Zeit den Zugang zu einer Session gewährt.

### Betriebliche Transparenz

Bei der Fehleranalyse geht es häufig um eine konkrete Session, zu deren Verhalten Fragen an den Betrieb gelangen. Um die Kommunikation zu vereinfachen und gleichzeitig die Connection-Keys geheim zu halten wird mit dem Protkoll V1.2 eine Session-Nummer eingeführt. Die Session wird in den Logs des CCC-Servers und der V1.2-Clients jeweils über die Session-Nr ausgegeben. Zusätzlich soll sie im Web GIS Client an geeigneter Stelle vom Benutzer eingesehen werden können.

Die Session-Nummern starten jeden Morgen bei 1, da der CCC-Service alle am frühen Morgen noch bestehenden Sessions "killt".

### Lösungsansatz für Sicherheit und Transparenz

Der CCC-Server führt neu für jede Session vier Informationen:

```json
{
    "sessionKey": "{d9e1e112-73f2-4797-9346-35fce16b6c40}",
    "sessionNr": 3,
    "gisKey": "U2FsdGVkX1+Hd3MABxVKN7RGzwLHBOydEWeFwyaV7PE=",
    "appKey": "U2FsdGVkX1+XA0aX3ZHvfCuI3TKx4H8OXTvBTVMv3og=",
}
```

* session_key: UUID, welche die Fachapplikation beim Aufruf des Web GIS Client übermittelt. Ist nur während dem "Handshake" relevant und bleibt über die ganze Lebensdauer der Session gleich.
* session_nr: Einfache Ganzzahl, zum einfachen Identifizieren der Session bei einer Problemanalyse. Bleibt ebenfalls über die ganze Lebensdauer der Session gleich.
* gis_key: Flüchtiger kryptografischer Hash der Verbindung gis - ccc.
* app_key: Füchtiger kryptografischer Hash der Verbindung (fach)app - ccc.

## reconnect

### Aufbau der Nachricht

```json
{
    "method": "reconnectGis|reconnectApp",
    "oldConnectionKey": "U2FsdGVkX1+XA0aX3ZHvfCuI3TKx4H8OXTvBTVMv3og=",
    "oldSessionNumber":2
}
```

Falls das reconnect vom CCC-Server akzeptiert wird, sendet dieser in der Folge eine keyChange-Anforderung an den Client.

## keyChange

### Aufbau der Nachricht

```json
{
    "method": "keyChange",
    "newConnectionKey": "U2FsdGVkX1+XA0aX3ZHvfCuI3TKx4H8OXTvBTVMv3og=",
}
```

Die keyChange-Anforderung wird vom CCC-Server periodisch (Beispielsweise alle 10 Minuten) oder als Reaktion auf eine reconnect-Anforderung ausgelöst.

## Erweiterung notifySessionReady

Via notifySessionReady V1.2 wird den Clients ihr erster Session-Key sowie die Session-Nummer mitgeteilt.

### Aufbau der Nachricht

```json
{
    "apiVersion": "1.2",
    "method": "notifySessionReady",
    "connectionKey": "U2FsdGVkX1+XA0aX3ZHvfCuI3TKx4H8OXTvBTVMv3og=",
    "sessionNr": 3
}
```

## connectGis und connectApp für V1.2 Clients

Die Struktur dieser Nachrichten bleibt identisch. Beim Aufruf eines V1.2-Fähigen Clients wird neu 1.2  als "apiVersion" übergeben.

## Rückwärtskompatibilität

Die Erweiterung ist für V 1.0 Clients transparent. Diese laufen ohne Anpassung weiter.

## Konsequenzen für SEIN

Die Identifier aller von SEIN "fernzusteuernden" Ebenen müssen in SEIN durch den "ARP-Superuser" konfiguriert werden können.
