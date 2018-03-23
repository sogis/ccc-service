package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonConverter {

    /**
     * Convert a message-object to a JSON-string
     * @param msg message-object
     * @return Nothing
     * @throws JsonProcessingException
     */
    public String messageToString(AbstractMessage msg) throws JsonProcessingException{
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = null;
        jsonString = mapper.writeValueAsString(msg);
        return jsonString;
    }

    /**
     * Convert a JSON-string to a message-object.
     * @param str JSON-string
     * @return Nothing
     * @throws IOException
     * @throws MethodeException
     */
    public AbstractMessage stringToMessage(String str) throws IOException, MethodeException {

         ObjectMapper mapper = new ObjectMapper();
         JsonNode obj = mapper.readTree(str);
         try {
             String method = obj.get("method").asText();
         } catch (NullPointerException e) {
             throw new MethodeException("No methode found in given JSON");
         }
         if (obj.get("method").asText().equals("appConnect")) {
            AppConnectMessage appConnectMessage  = new AppConnectMessage();
            appConnectMessage.setApiVersion(obj.get("apiVersion").asText());
            appConnectMessage.setClientName(obj.get("clientName").asText());
            appConnectMessage.setSession(new SessionId(obj.get("session").asText()));
            return appConnectMessage;
         }
         if (obj.get("method").asText().equals("gisConnect")) {
             GisConnectMessage gisConnectMessage  = new GisConnectMessage();
             gisConnectMessage.setApiVersion(obj.get("apiVersion").asText());
             gisConnectMessage.setClientName(obj.get("clientName").asText());
             gisConnectMessage.setSession(new SessionId(obj.get("session").asText()));
             return gisConnectMessage;
         }
         if (obj.get("method").asText().equals("ready")) {
             ReadyMessage readyMessage = new ReadyMessage();
             readyMessage.setApiVersion(obj.get("apiVersion").asText());
             return readyMessage;
         }
         if (obj.get("method").asText().equals("create")) {
             CreateMessage createMessage = new CreateMessage();
             createMessage.setContext(obj.get("context"));
             createMessage.setZoomTo(obj.get("zoomto"));
             return createMessage;
         }
         if (obj.get("method").asText().equals("edit")) {
             EditMessage editMessage = new EditMessage();
             editMessage.setContext(obj.get("context"));
             editMessage.setData(obj.get("data"));
             return editMessage;
         }
         if (obj.get("method").asText().equals("show")) {
             ShowMessage showMessage = new ShowMessage();
             showMessage.setContext(obj.get("context"));
             showMessage.setData(obj.get("data"));
             return showMessage;
         }
         if (obj.get("method").asText().equals("cancel")) {
             CancelMessage cancelMessage = new CancelMessage();
             cancelMessage.setContext(obj.get("contect"));
             return cancelMessage;
         }
         if (obj.get("method").asText().equals("changed")) {
             ChangedMessage changedMessage = new ChangedMessage();
             changedMessage.setContext(obj.get("context"));
             changedMessage.setData(obj.get("data"));
             return changedMessage;
         }
         if (obj.get("method").asText().equals("dataWritten")) {
             DataWrittenMessage dataWrittenMessage = new DataWrittenMessage();
             dataWrittenMessage.setProperties(obj.get("properties"));
             return dataWrittenMessage;
         }
         if (obj.get("method").asText().equals("selected")) {
             SelectedMessage selectedMessage = new SelectedMessage();
             selectedMessage.setContext_list(obj.get("context_list"));
             return selectedMessage;
         }
         else {
             throw new MethodeException("No suitable methode found in given JSON");
         }
    }
}