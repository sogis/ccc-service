package ch.so.agi.cccservice.messages;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import ch.so.agi.cccservice.SessionId;

public class ConnectAppMessage extends AbstractMessage {
    public static final String METHOD_NAME = "connectApp";
    @JsonUnwrapped
   private SessionId session;
   private String clientName;
   private String apiVersion;

   public ConnectAppMessage() {
      super(METHOD_NAME);
   }

   public SessionId getSession() {
      return session;
   }

   public void setSession(SessionId session) {
      this.session = session;
   }

   public String getClientName() {
      return clientName;
   }

   public void setClientName(String clientName) {
      this.clientName = clientName;
   }

   public String getApiVersion() {
      return apiVersion;
   }

   public void setApiVersion(String apiVersion) {
      this.apiVersion = apiVersion;
   }
}
