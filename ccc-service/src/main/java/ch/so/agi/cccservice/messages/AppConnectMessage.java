package ch.so.agi.cccservice.messages;

import ch.so.agi.cccservice.SessionId;

public class AppConnectMessage extends AbstractMessage {
   private SessionId session;
   private String clientName;
   private String apiVersion;
}
