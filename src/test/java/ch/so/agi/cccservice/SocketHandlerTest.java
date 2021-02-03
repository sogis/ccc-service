package ch.so.agi.cccservice;

import ch.so.agi.cccservice.SessionId;
import ch.so.agi.cccservice.messages.AbstractMessage;
import ch.so.agi.cccservice.messages.ConnectAppMessage;
import ch.so.agi.cccservice.messages.ConnectGisMessage;
import ch.so.agi.cccservice.messages.NotifyErrorMessage;
import ch.so.agi.cccservice.messages.NotifyGeoObjectSelectedMessage;
import ch.so.agi.cccservice.messages.NotifySessionReadyMessage;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(org.springframework.test.context.junit4.SpringRunner.class)
public class SocketHandlerTest {
    
    public static final String GIS_CLIENT_NAME = "SocketHandlerTest GIS";
    public static final String CCC_PROTOCOL_VERSION = "1.0";
    public static final String APP_CLIENT_NAME = "SocketHandlerTest APP";
    private static final String DEFAULT_ENDPOINT = "ws://localhost:8080/ccc-service";
    private static final long MAX_INACTIVITY_TIME=20;
    private static final long MAX_PAIRING_TIME=10;
  
    Logger logger = LoggerFactory.getLogger(SocketHandlerTest.class);
    String endpoint=DEFAULT_ENDPOINT;

    @BeforeClass
    static public void startService() {
        
        System.setProperty(Service.CCC_MAX_INACTIVITY, Long.toString(MAX_INACTIVITY_TIME));
        System.setProperty(Service.CCC_MAX_PAIRING, Long.toString(MAX_PAIRING_TIME));
        SpringApplication.run(Application.class, new String[0]);

        
    }
    
    public class ClientSocketHandler implements WebSocketHandler {

        Logger logger = LoggerFactory.getLogger(SocketHandler.class);

        Boolean appReady = null;
        private String clientName;
        private SessionId sessionId;
        private WebSocketSession webSocketSession;

        public ClientSocketHandler(String appClientName) {
            this.clientName=appClientName;
        }

        public Boolean getAppReady() {
            return appReady;
        }

        /**
         * Set supportsPartialMessages to false
         * @return false
         */
        @Override
        public boolean supportsPartialMessages() {
            return false;
        }

        /**
         * Handle an error from the underlying WebSocket message transport and write it into the error-log.
         * @param session: The WebSocketSession where the error occurred
         * @param exception: The thrown Error
         * @throws Exception
         */
        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            logger.error("Got a handleTransportError ", exception);
        }

        /**
         * Handles an incoming JSON message.
         * @param session The WebSocketSession
         * @param message The incoming message. Allows only the method "ready". "error" and anything else set appReady to "false"
         * @throws Exception
         */
        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode obj = mapper.readTree(message.getPayload().toString());
            String method;
            try {
                method = obj.get("method").asText();
            } catch (NullPointerException e) {
                throw new ServiceException(400, "No method found in given JSON");
            }

            if (method.equals(NotifySessionReadyMessage.METHOD_NAME)) {
                logger.info(clientName+" "+method+" received");
                appReady = true;
            }
            else if (method.equals(NotifyErrorMessage.METHOD_NAME)) {
                logger.error("Got Error: "+obj.get("message").asText());
                appReady = false;
            }
            else {
                logger.error("Did not get correct message. Got: "+message.getPayload());
                appReady = false;
            }
        }

        /**
         *Invoked after WebSocket negotiation has succeeded and the WebSocket connection is opened and ready for use.
         * @param session The WebSocketSession
         * @throws Exception
         */
        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            webSocketSession = session;
        }

        /**
         * Invoked after the WebSocket connection has been closed by either side, or after a transport error has occurred.
         * Although the session may technically still be open, depending on the underlying implementation,
         * sending messages at this point is discouraged and most likely will not succeed.
         * @param session The WebSocketSession
         * @param closeStatus The close status
         * @throws Exception
         */
        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
            logger.info(clientName+": connection closed!");
        }

        /**
         * Converts a message (class) to a string and send the resulting JSON to the WebSocket
         * @param msg The message (class)
         * @throws Exception
         */
        public void sendMessage(AbstractMessage msg) throws Exception {
            JsonConverter jsonConverter = new JsonConverter();
            String resultingJson = jsonConverter.messageToString(msg);
            webSocketSession.sendMessage(new TextMessage(resultingJson));
        }

        /**
         * Checks if the connection is open or not.
         * @param webSocketHandler: The webSocketHandler
         * @return true or false (boolean)
         */
        public boolean isConnected() {
            return webSocketSession!=null && SessionPool.isSocketOpen(webSocketSession);
        }
        public void closeConnection() throws IOException
        {
            if(isConnected()) {
                webSocketSession.close();
            }
        }
    }
    
    @Test
    public void simpleHappyFlow() throws Exception {

        StandardWebSocketClient appClient = new StandardWebSocketClient();
        ClientSocketHandler appSessionHandler = new ClientSocketHandler(APP_CLIENT_NAME);
        appClient.doHandshake(appSessionHandler,endpoint);

        Thread.sleep(2000);
        assertTrue(appSessionHandler.isConnected());
        
        SessionId sessionId = new SessionId("{"+UUID.randomUUID().toString()+"}");
        
        ConnectAppMessage appConnectMessage = new ConnectAppMessage();
        appConnectMessage.setApiVersion(CCC_PROTOCOL_VERSION);
        appConnectMessage.setSession(sessionId);
        appConnectMessage.setClientName(APP_CLIENT_NAME);
        appSessionHandler.sendMessage(appConnectMessage);


        StandardWebSocketClient gisClient = new StandardWebSocketClient();
        ClientSocketHandler gisSessionHandler = new ClientSocketHandler(GIS_CLIENT_NAME);
        gisClient.doHandshake(gisSessionHandler,endpoint);

        Thread.sleep(2000);
        assertTrue(gisSessionHandler.isConnected());

        ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
        gisConnectMessage.setApiVersion(CCC_PROTOCOL_VERSION);
        gisConnectMessage.setSession(sessionId);
        gisConnectMessage.setClientName(GIS_CLIENT_NAME);
        gisSessionHandler.sendMessage(gisConnectMessage);

        Thread.sleep(2000);
        assertTrue(gisSessionHandler.getAppReady() != null && gisSessionHandler.getAppReady() == true);
        assertTrue(appSessionHandler.getAppReady() != null && appSessionHandler.getAppReady() == true);

    }
    @Test
    public void parallelSessionsWithTimeouts()
    throws Exception
    {
        // session 1: connectApp, connectGis, disconnect gis client, assert app client is disconnected
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    StandardWebSocketClient appClient = new StandardWebSocketClient();
                    ClientSocketHandler appSessionHandler = new ClientSocketHandler(APP_CLIENT_NAME);
                    appClient.doHandshake(appSessionHandler,endpoint);

                    Thread.sleep(2000);
                    assertTrue(appSessionHandler.isConnected());
                    
                    SessionId sessionId = new SessionId("{"+UUID.randomUUID().toString()+"}");
                    
                    ConnectAppMessage appConnectMessage = new ConnectAppMessage();
                    appConnectMessage.setApiVersion(CCC_PROTOCOL_VERSION);
                    appConnectMessage.setSession(sessionId);
                    appConnectMessage.setClientName(APP_CLIENT_NAME);
                    appSessionHandler.sendMessage(appConnectMessage);


                    StandardWebSocketClient gisClient = new StandardWebSocketClient();
                    ClientSocketHandler gisSessionHandler = new ClientSocketHandler(GIS_CLIENT_NAME);
                    gisClient.doHandshake(gisSessionHandler,endpoint);

                    Thread.sleep(2000);
                    assertTrue(gisSessionHandler.isConnected());

                    ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
                    gisConnectMessage.setApiVersion(CCC_PROTOCOL_VERSION);
                    gisConnectMessage.setSession(sessionId);
                    gisConnectMessage.setClientName(GIS_CLIENT_NAME);
                    gisSessionHandler.sendMessage(gisConnectMessage);

                    Thread.sleep(2000);
                    assertTrue(gisSessionHandler.getAppReady() != null && gisSessionHandler.getAppReady() == true);
                    assertTrue(appSessionHandler.getAppReady() != null && appSessionHandler.getAppReady() == true);
                    // end of setup session 1
                    
                    gisSessionHandler.closeConnection();
                    Thread.sleep(1000);
                    assertFalse(gisSessionHandler.isConnected());
                    assertFalse(appSessionHandler.isConnected());
                    
                }catch(Exception e) {
                    throw new IllegalStateException(e);
                }
            }
            
        }).start();
        // session 2: connectGis, assert after CCC_MAX_UNJOINED expiry that gis client is disconnected
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    SessionId sessionId = new SessionId("{"+UUID.randomUUID().toString()+"}");

                    StandardWebSocketClient gisClient = new StandardWebSocketClient();
                    ClientSocketHandler gisSessionHandler = new ClientSocketHandler(GIS_CLIENT_NAME);
                    gisClient.doHandshake(gisSessionHandler,endpoint);

                    Thread.sleep(2000);
                    assertTrue(gisSessionHandler.isConnected());

                    ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
                    gisConnectMessage.setApiVersion(CCC_PROTOCOL_VERSION);
                    gisConnectMessage.setSession(sessionId);
                    gisConnectMessage.setClientName(GIS_CLIENT_NAME);
                    gisSessionHandler.sendMessage(gisConnectMessage);

                    Thread.sleep(2000);
                    Thread.sleep(MAX_PAIRING_TIME*1000);
                    NotifyGeoObjectSelectedMessage notifyGeoObjectSelectedMessage=new NotifyGeoObjectSelectedMessage();
                    if(gisSessionHandler.isConnected()) {
                        gisSessionHandler.sendMessage(notifyGeoObjectSelectedMessage);
                    }
                    Thread.sleep(2000);
                    assertFalse(gisSessionHandler.isConnected());
                }catch(Exception e) {
                    throw new IllegalStateException(e);
                }
                
            }
            
        }).start();
        
        // session 3: connectApp, connectGis, assert after CCC_MAX_INACTIVITY expiry that both clients are disconnected
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    StandardWebSocketClient appClient = new StandardWebSocketClient();
                    ClientSocketHandler appSessionHandler = new ClientSocketHandler(APP_CLIENT_NAME);
                    appClient.doHandshake(appSessionHandler,endpoint);

                    Thread.sleep(2000);
                    assertTrue(appSessionHandler.isConnected());
                    
                    SessionId sessionId = new SessionId("{"+UUID.randomUUID().toString()+"}");
                    
                    ConnectAppMessage appConnectMessage = new ConnectAppMessage();
                    appConnectMessage.setApiVersion(CCC_PROTOCOL_VERSION);
                    appConnectMessage.setSession(sessionId);
                    appConnectMessage.setClientName(APP_CLIENT_NAME);
                    appSessionHandler.sendMessage(appConnectMessage);


                    StandardWebSocketClient gisClient = new StandardWebSocketClient();
                    ClientSocketHandler gisSessionHandler = new ClientSocketHandler(GIS_CLIENT_NAME);
                    gisClient.doHandshake(gisSessionHandler,endpoint);

                    Thread.sleep(2000);
                    assertTrue(gisSessionHandler.isConnected());

                    ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
                    gisConnectMessage.setApiVersion(CCC_PROTOCOL_VERSION);
                    gisConnectMessage.setSession(sessionId);
                    gisConnectMessage.setClientName(GIS_CLIENT_NAME);
                    gisSessionHandler.sendMessage(gisConnectMessage);

                    Thread.sleep(2000);
                    assertTrue(gisSessionHandler.getAppReady() != null && gisSessionHandler.getAppReady() == true);
                    assertTrue(appSessionHandler.getAppReady() != null && appSessionHandler.getAppReady() == true);
                    // end of setup session 1
                    
                    Thread.sleep(1000);
                    Thread.sleep(MAX_INACTIVITY_TIME*1000);
                    NotifyGeoObjectSelectedMessage notifyGeoObjectSelectedMessage=new NotifyGeoObjectSelectedMessage();
                    if(gisSessionHandler.isConnected()) {
                        gisSessionHandler.sendMessage(notifyGeoObjectSelectedMessage);
                    }
                    Thread.sleep(2000);
                    assertFalse(gisSessionHandler.isConnected());
                    assertFalse(appSessionHandler.isConnected());
                    
                }catch(Exception e) {
                    throw new IllegalStateException(e);
                }
                
            }
            
        }).start();

        Thread.sleep(MAX_INACTIVITY_TIME*1000);
        
    }
}