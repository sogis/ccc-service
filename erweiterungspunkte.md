# Weiterfahren

* Semantic versioning verstehen oder definieren
  * Diese Aenderung wäre wohl eher 1.1 als 2.0, da Rückwärtskompatibel mit V1 clients 
* docker image
  * Auf java 25 basierendes base image verwenden
  * Tag auf 2.x heben
* Dokumentieren
  * Grundsätzliche Zusammenarbeit der Packages und ihrer Hauptklassen 
  * Semantic versioning
  * Probe, falls noch nicht beschrieben
* Package cccservice_old löschen

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


diff --git a/src/main/java/ch/so/agi/cccservice/health/SocketClient.java b/src/main/java/ch/so/agi/cccservice/health/SocketClient.java
new file mode 100644
index 0000000000000000000000000000000000000000..09911d16c2dbdc0ba66daa4515b4f2da07356d37
--- /dev/null
+++ b/src/main/java/ch/so/agi/cccservice/health/SocketClient.java
@@ -0,0 +1,135 @@
+package ch.so.agi.cccservice.health;
+
+import ch.so.agi.cccservice.message.Error;
+import org.slf4j.Logger;
+import org.slf4j.LoggerFactory;
+import org.springframework.web.socket.TextMessage;
+import org.springframework.web.socket.WebSocketSession;
+import org.springframework.web.socket.client.standard.StandardWebSocketClient;
+import org.springframework.web.socket.handler.TextWebSocketHandler;
+
+import com.fasterxml.jackson.databind.ObjectMapper;
+import com.fasterxml.jackson.databind.node.ObjectNode;
+
+import java.io.IOException;
+import java.util.UUID;
+import java.util.concurrent.ExecutionException;
+
+/**
+ * WebSocket client used by health checks to interact with the CCC service.
+ * Provides basic helpers to connect, reconnect and send error notifications
+ * as either an APP or GIS client.
+ */
  +public class SocketClient extends TextWebSocketHandler {
+    private static final Logger log = LoggerFactory.getLogger(SocketClient.class);
+
+    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
+
+    public enum ClientType {
+        APP("connectApp", "reconnectApp"),
+        GIS("connectGis", "reconnectGis");
+
+        private final String connectMethod;
+        private final String reconnectMethod;
+
+        ClientType(String connectMethod, String reconnectMethod) {
+            this.connectMethod = connectMethod;
+            this.reconnectMethod = reconnectMethod;
+        }
+    }
+
+    private final StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
+    private final String baseAddress;
+    private final ClientType clientType;
+
+    private WebSocketSession session;
+
+    public SocketClient(String baseAddress, ClientType clientType) {
+        this.baseAddress = baseAddress;
+        this.clientType = clientType;
+    }
+
+    @Override
+    public void afterConnectionEstablished(WebSocketSession session) {
+        this.session = session;
+    }
+
+    @Override
+    public void handleTransportError(WebSocketSession session, Throwable exception) {
+        log.warn("Transport error on websocket client", exception);
+    }
+
+    @Override
+    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
+        this.session = null;
+    }
+
+    public synchronized void connect(UUID sessionUid, String clientName, String apiVersion) {
+        WebSocketSession webSocketSession = ensureConnected();
+
+        ObjectNode connectPayload = OBJECT_MAPPER.createObjectNode();
+        connectPayload.put("method", clientType.connectMethod);
+        connectPayload.put("session", formatSessionUid(sessionUid));
+        connectPayload.put("clientName", clientName);
+        connectPayload.put("apiVersion", apiVersion);
+
+        sendPayload(webSocketSession, connectPayload);
+    }
+
+    public synchronized void reconnect(String oldConnectionKey, int oldSessionNumber) {
+        WebSocketSession webSocketSession = ensureConnected();
+
+        ObjectNode reconnectPayload = OBJECT_MAPPER.createObjectNode();
+        reconnectPayload.put("method", clientType.reconnectMethod);
+        reconnectPayload.put("oldConnectionKey", oldConnectionKey);
+        reconnectPayload.put("oldSessionNumber", oldSessionNumber);
+
+        sendPayload(webSocketSession, reconnectPayload);
+    }
+
+    public synchronized void sendError(int code, String message, String nativeCode, String technicalDetails) {
+        WebSocketSession webSocketSession = ensureConnected();
+
+        ObjectNode errorPayload = OBJECT_MAPPER.createObjectNode();
+        errorPayload.put("method", Error.MESSAGE_TYPE);
+        errorPayload.put("code", code);
+        errorPayload.put("message", message);
+
+        if (nativeCode != null) {
+            errorPayload.put("nativeCode", nativeCode);
+        }
+
+        if (technicalDetails != null) {
+            errorPayload.put("technicalDetails", technicalDetails);
+        }
+
+        sendPayload(webSocketSession, errorPayload);
+    }
+
+    private WebSocketSession ensureConnected() {
+        try {
+            if (session == null || !session.isOpen()) {
+                session = webSocketClient.doHandshake(this, baseAddress).get();
+            }
+        } catch (InterruptedException e) {
+            Thread.currentThread().interrupt();
+            throw new IllegalStateException("Interrupted while establishing websocket connection", e);
+        } catch (ExecutionException e) {
+            throw new IllegalStateException("Could not establish websocket connection", e);
+        }
+
+        return session;
+    }
+
+    private void sendPayload(WebSocketSession webSocketSession, ObjectNode payload) {
+        try {
+            webSocketSession.sendMessage(new TextMessage(payload.toString()));
+        } catch (IOException e) {
+            throw new IllegalStateException("Could not send payload to cccservice", e);
+        }
+    }
+
+    private String formatSessionUid(UUID sessionUid) {
+        return "{" + sessionUid + "}";
+    }
     +}



