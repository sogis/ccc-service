package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonConverter {

    /**
     * Convert a message-object to a JSON-string
     * @param msg message-object
     * @return Nothing
     * @throws JsonProcessingException
     */
    public String messageToString(AbstractMessage msg) throws JsonProcessingException{
        ObjectMapper mapper = new ObjectMapper();
        String jsonString;
        jsonString = mapper.writeValueAsString(msg);
        return jsonString;
    }

    /**
     * Convert a JSON-string to a message-object.
     * @param str JSON-string
     * @return Nothing
     * @throws IOException, ServiceException
     */
    public AbstractMessage stringToMessage(String str) throws IOException, ServiceException {

         ObjectMapper mapper = new ObjectMapper();
         JsonNode obj = mapper.readTree(str);
         String method;
         try {
             method = obj.get("method").asText();
         } catch (NullPointerException e) {
             throw new ServiceException(400, "No method found in given JSON");
         }
         try {
             if (method.equals("appConnect")) {
                 AppConnectMessage appConnectMessage = new AppConnectMessage();
                 appConnectMessage.setApiVersion(obj.get("apiVersion").asText());
                 appConnectMessage.setClientName(obj.get("clientName").asText());
                 appConnectMessage.setSession(new SessionId(obj.get("session").asText()));
                 if (appConnectMessage.getSession() == null || appConnectMessage.getApiVersion() == null ||
                         appConnectMessage.getClientName() == null){
                     throw new ServiceException(400, "Attribute in appConnect is missing or wrong.");
                 }
                 return appConnectMessage;
             }
             if (method.equals("gisConnect")) {
                 GisConnectMessage gisConnectMessage = new GisConnectMessage();
                 gisConnectMessage.setApiVersion(obj.get("apiVersion").asText());
                 gisConnectMessage.setClientName(obj.get("clientName").asText());
                 gisConnectMessage.setSession(new SessionId(obj.get("session").asText()));
                 if (gisConnectMessage.getSession() == null || gisConnectMessage.getApiVersion() == null ||
                         gisConnectMessage.getClientName() == null){
                     throw new ServiceException(400, "Attribute in gisConnect is missing or wrong.");
                 }
                 return gisConnectMessage;
             }
             if (method.equals("ready")) {
                 ReadyMessage readyMessage = new ReadyMessage();
                 readyMessage.setApiVersion(obj.get("apiVersion").asText());
                 if (readyMessage.getApiVersion() == null){
                     throw new ServiceException(400, "Attribute apiVersion is missing or wrong.");
                 }
                 return readyMessage;
             }
             if (method.equals("create")) {
                 CreateMessage createMessage = new CreateMessage();
                 createMessage.setContext(obj.get("context"));
                 createMessage.setZoomTo(obj.get("zoomTo"));
                 if (createMessage.getContext() == null) {
                     throw new ServiceException(400, "Attribut context is missing or wrong.");
                 }
                 return createMessage;
             }
             if (method.equals("edit")) {
                 EditMessage editMessage = new EditMessage();
                 editMessage.setContext(obj.get("context"));
                 editMessage.setData(obj.get("data"));
                 if (editMessage.getContext() == null || editMessage.getData() == null){
                     throw new ServiceException(400, "Attribute in editMessage is missing or wrong.");
                 }
                 return editMessage;
             }
             if (method.equals("show")) {
                 ShowMessage showMessage = new ShowMessage();
                 showMessage.setContext(obj.get("context"));
                 showMessage.setData(obj.get("data"));
                 if (showMessage.getContext() == null || showMessage.getData() == null){
                     throw new ServiceException(400, "Attribute in showMessage is missing or wrong.");
                 }
                 return showMessage;
             }
             if (method.equals("cancel")) {
                 CancelMessage cancelMessage = new CancelMessage();
                 cancelMessage.setContext(obj.get("context"));
                 if (cancelMessage.getContext() == null){
                     throw new ServiceException(400, "Attribute context is missing or wrong.");
                 }
                 return cancelMessage;
             }
             if (method.equals("changed")) {
                 ChangedMessage changedMessage = new ChangedMessage();
                 changedMessage.setContext(obj.get("context"));
                 changedMessage.setData(obj.get("data"));
                 if (changedMessage.getContext() == null){
                     throw new ServiceException(400, "Attribute context is missing or wrong.");
                 }
                 return changedMessage;
             }
             if (method.equals("dataWritten")) {
                 DataWrittenMessage dataWrittenMessage = new DataWrittenMessage();
                 dataWrittenMessage.setProperties(obj.get("properties"));
                 if (dataWrittenMessage.getProperties() == null){
                     throw new ServiceException(400, "Attribute properties is missing or wrong.");
                 }
                 return dataWrittenMessage;
             }
             if (method.equals("error")){
                 ErrorMessage errorMessage = new ErrorMessage();
                 errorMessage.setCode(obj.get("code").asInt());
                 errorMessage.setMessage(obj.get("message").asText());
                 errorMessage.setUserData(obj.get("userData"));
                 errorMessage.setNativeCode(obj.get("nativeCode").asText());
                 errorMessage.setTechnicalDetails(obj.get("technicalDetails").asText());
                 if (errorMessage.getCode() == 0 || errorMessage.getMessage() == null){
                    throw new ServiceException(400, "Attribute in errorMessage missing or wrong");
                 }
                 return errorMessage;
             }
             if (method.equals("selected")) {
                 SelectedMessage selectedMessage = new SelectedMessage();
                 selectedMessage.setContext_list(obj.get("context_list"));
                 return selectedMessage;
             } else {
                 throw new IOException("No suitable methode found in given JSON. Given method: "+obj.get("method").asText());
             }
         } catch (NullPointerException e) {
             throw new ServiceException(400, "One or more arguments missing for given method!");
         }
    }
}