package ch.so.agi.cccprobe;

import java.util.Scanner;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

public class SimpleClient {
    public static void main(String[] args) {
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketHandler sessionHandler = new WebSocketHandler() {

            @Override
            public boolean supportsPartialMessages() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                // TODO Auto-generated method stub

            }

            @Override
            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
                // TODO Auto-generated method stub
                System.out.println("Message received: " + message.getPayload());

            }

            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                // TODO Auto-generated method stub
                session.sendMessage(new TextMessage("You are now connected to the server. This is the first message."));
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

            }
        };
        client.doHandshake(sessionHandler,"ws://localhost/");
        client.doHandshake(sessionHandler,"ws://localhost/myHandler");

        new Scanner(System.in).nextLine(); // Don't close immediately.
    }
}