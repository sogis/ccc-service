# Übersicht

## Funktionsweise

![Uebersicht.puml](docs/res/overview.png)

Der CCC-Service verbindet als "Mittelsmann" GIS-unwillige Fachapplikationen
mit den Kartenapplikationen des AGI.

Mittels der "INIT"-Nachrichten wird dabei in der User-Session das Pairing für 
einen User zwischen der Fachapplikation und der entsprechenden GIS-Applikation initialisiert.

Die Session stellt sicher, dass die Nachrichten für den Benutzer korrekt von der 
Fachapplikation an die Kartenapplikation des AGI weitergereicht wird.

Die Kommunikation erfolgt bidirektional über Web Socket. Sprich die Fach- und 
Kartenapplikationen sind sowohl Sender wie auch Empfänger von Nachrichten.

Der Nachrichteninhalt und -Umfang richtet sich nach den Anwendungsfällen, welche
eine Fachapplikation typischerweise auf der Karte umsetzen lassen will.

## Übersicht der Nachrichten  

|Name|Richtung|Typ|Beschreibung|
|---|---|---|---|
|connectApp|F > CCC|INIT|Anfrage der Fachapplikation, in die entsprechende Session aufgenommen zu werden.|
|connectGis|K > CCC|INIT|Anfrage der Kartenapplikation, in die entsprechende Session aufgenommen zu werden.|
|notifySessionReady|CCC > F,K|INIT|Nachricht des CCC-Service an Fach- und Kartenapplikation, dass beide Seiten der Session beigetreten sind, und nun "RUN"-Nachrichten ausgetauscht werden können.|
|createGeoObject|F > K|RUN|Versetzt den Web GIS Client, nach Zoom auf den entsprechenden Ort, in den Editiermodus. Anschliessend erfasst der Benutzer an der entsprechenden Stelle die neue Geometrie.|
|editGeoObject|F > K|RUN|Mit dieser Nachricht wird die bestehende Geometrie eines Fachobjektes im GIS editiert. Verhalten des Web GIS Client analog zu «createGeoObject».|
|showGeoObject|F > K|RUN|Bewirkt das zentrierte und selektierte Anzeigen des übergebenen Fachobjektes in der Karte des Web GIS Client.|
|cancelEditGeoObject|F > K|RUN|Aufforderung an die Kartenapplikation, den Edit-Status zu beenden.|
|notifyObjectUpdated|F > K|RUN|Benachrichtigung, dass Informationen eines Fachobjektes geändert wurde --> Kartenapplikation lädt darauf Beispielsweise die Karte neu.|
|notifyEditGeoObjectDone|K > F|RUN|Mit dieser Nachricht sendet der Web GIS Client nach dem Beenden des Editierens die erfasste / geänderte Geometrie des Fachobjektes an die Fachapplikation.|
|notifyGeoObjectSelected|K > F|RUN|Nach Empfang dieser Nachricht zeigt die Fachapplikation die Informationen des auf der Karte selektierten Fachobjektes an.|
|notifyError|Tri-Dir|ERR|Damit werden Fach- und Kartenapplikation über Fehler benachrichtigt. Absender ist bei Protokollfehlern der CCC-Service, bei Verarbeitungsfehlern die Fachapplikation oder die Kartenapplikation.|

## Detailinformationen zum Protokoll und den jeweiligen Nachrichten

[CCC-Service Spezifikation Version 1.0](docs/res/Spezifikation_CCC_Schnittstelle_V1.0.pdf)

<!--
Interne Bemerkung: Die Originaldatei des Spezifikation (Word) liegt absichtlich nicht bei, da die
Formattierung dieser durch Vermischung der Layout-Vorschriften mehrerer Word-Templates unbrauchbar wurde.

Bei Weiterentwicklung des Protokolles in Richtung V 1.1 entsprechend zuerst den Inhalt des PDF zuerst nach Markdown migrieren,
und anschliessend die Ergänzungen vornehmen. 
-->

## Informationen zur Ausführung des Docker Image und zum Sourcecode

[docs/user](docs/user/index.md) How to run the ccc-service and/or the probe-tool

[docs/dev](docs/dev/index.md) Developer specific documentation about the ccc-service, such as class structure, how to build, etc. 