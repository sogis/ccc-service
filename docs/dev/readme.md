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
  * Gibt auf dem Root-Pfad als Antwort auf ein HTTP-Get die Version des CCC-Service und die Anzahl der registrierten Sessions aus.
  * Default-Pfad: http://localhost:8080
* LivenessProbe (Package ch.so.agi.cccservice.health): 
  * Prüft die Gesundheit des Service durch Verschicken von Testnachrichten von einem Test-Client.
  * Default-Pfad: http://localhost:8080/actuator/health