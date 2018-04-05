package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Allows transformation from a abstractMessage to String and vice versa
 */
@Component
public class JsonConverter {

    /**
     * Convert a message-object to a JSON-string
     * @param msg message-object
     * @return Message as string
     * @throws JsonProcessingException if Message could not be converted to string
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
        checkNodeTypeString(obj, "method");
        checkNodeTypeString(obj, "apiVersion" );
        checkNodeTypeString(obj, "clientName" );
        checkNodeTypeString(obj, "session" );
        checkNodeTypeObject(obj, "context");
        checkNodeTypeObject(obj, "zoomTo" );
        checkNodeTypeObject(obj, "data" );
        checkNodeTypeObject(obj, "properties" );
        checkNodeTypeArrayOrObject(obj, "context_list" );
        checkNodeTypeInteger(obj, "code" );
        checkNodeTypeString(obj, "message" );
        checkNodeTypeObject(obj, "userData" );
        checkNodeTypeString(obj, "nativeCode" );
        checkNodeTypeString(obj, "technicalDetails" );



        try {
             if (method.equals(ConnectAppMessage.METHOD_NAME)) {
                 ConnectAppMessage appConnectMessage = new ConnectAppMessage();
                 appConnectMessage.setApiVersion(obj.get("apiVersion").asText());
                 appConnectMessage.setClientName(obj.get("clientName").asText());
                 appConnectMessage.setSession(new SessionId(obj.get("session").asText()));
                 if (appConnectMessage.getSession() == null || appConnectMessage.getApiVersion() == null
                         || appConnectMessage.getClientName() == null){
                     throw new ServiceException(400, "Attribute in appConnect is missing or wrong.");
                 }
                 return appConnectMessage;
             }
             if (method.equals(ConnectGisMessage.METHOD_NAME)) {
                 ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
                 gisConnectMessage.setApiVersion(obj.get("apiVersion").asText());
                 gisConnectMessage.setClientName(obj.get("clientName").asText());
                 gisConnectMessage.setSession(new SessionId(obj.get("session").asText()));
                 if (gisConnectMessage.getSession() == null || gisConnectMessage.getApiVersion() == null ||
                         gisConnectMessage.getClientName() == null){
                     throw new ServiceException(400, "Attribute in gisConnect is missing or wrong.");
                 }
                 return gisConnectMessage;
             }
             if (method.equals(NotifySessionReadyMessage.METHOD_NAME)) {
                 NotifySessionReadyMessage readyMessage = new NotifySessionReadyMessage();
                 readyMessage.setApiVersion(obj.get("apiVersion").asText());
                 if (readyMessage.getApiVersion() == null){
                     throw new ServiceException(400, "Attribute apiVersion is missing or wrong.");
                 }
                 return readyMessage;
             }
             if (method.equals(CreateGeoObjectMessage.METHOD_NAME)) {
                 CreateGeoObjectMessage createMessage = new CreateGeoObjectMessage();
                 createMessage.setContext(obj.get("context"));
                 createMessage.setZoomTo(obj.get("zoomTo"));
                 if (createMessage.getContext() == null) {
                     throw new ServiceException(400, "Attribut context is missing or wrong.");
                 }
                 return createMessage;
             }
             if (method.equals(EditGeoObjectMessage.METHOD_NAME)) {
                 EditGeoObjectMessage editMessage = new EditGeoObjectMessage();
                 editMessage.setContext(obj.get("context"));
                 editMessage.setData(obj.get("data"));
                 if (editMessage.getContext() == null || editMessage.getData() == null){
                     throw new ServiceException(400, "Attribute in editMessage is missing or wrong.");
                 }
                 return editMessage;
             }
             if (method.equals(ShowGeoObjectMessage.METHOD_NAME)) {
                 ShowGeoObjectMessage showMessage = new ShowGeoObjectMessage();
                 showMessage.setContext(obj.get("context"));
                 showMessage.setData(obj.get("data"));
                 if (showMessage.getContext() == null || showMessage.getData() == null){
                     throw new ServiceException(400, "Attribute in showMessage is missing or wrong.");
                 }
                 return showMessage;
             }
             if (method.equals(CancelEditGeoObjectMessage.METHOD_NAME)) {
                 CancelEditGeoObjectMessage cancelMessage = new CancelEditGeoObjectMessage();
                 cancelMessage.setContext(obj.get("context"));
                 if (cancelMessage.getContext() == null){
                     throw new ServiceException(400, "Attribute context is missing or wrong.");
                 }
                 return cancelMessage;
             }
             if (method.equals(NotifyEditGeoObjectDoneMessage.METHOD_NAME)) {
                 NotifyEditGeoObjectDoneMessage changedMessage = new NotifyEditGeoObjectDoneMessage();
                 changedMessage.setContext(obj.get("context"));
                 changedMessage.setData(obj.get("data"));
                 if (changedMessage.getContext() == null){
                     throw new ServiceException(400, "Attribute context is missing or wrong.");
                 }
                 return changedMessage;
             }
             if (method.equals(NotifyObjectUpdatedMessage.METHOD_NAME)) {
                 NotifyObjectUpdatedMessage dataWrittenMessage = new NotifyObjectUpdatedMessage();
                 dataWrittenMessage.setProperties(obj.get("properties"));
                 if (dataWrittenMessage.getProperties() == null){
                     throw new ServiceException(400, "Attribute properties is missing or wrong.");
                 }
                 return dataWrittenMessage;
             }
             if (method.equals(NotifyErrorMessage.METHOD_NAME)){
                 NotifyErrorMessage errorMessage = new NotifyErrorMessage();
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
             if (method.equals(NotifyGeoObjectSelectedMessage.METHOD_NAME)) {
                 NotifyGeoObjectSelectedMessage selectedMessage = new NotifyGeoObjectSelectedMessage();
                 selectedMessage.setContext_list(obj.get("context_list"));
                 return selectedMessage;
             } else {
                 throw new IOException("No suitable methode found in given JSON. Given method: "+obj.get("method").asText());
             }
         } catch (NullPointerException e) {
             throw new ServiceException(400, "One or more arguments missing for given method!");
         }
    }

    /**
     * Checks if a json node is of type String
     * @param obj as JsonNode
     * @param attr (property) as String
     * @throws ServiceException if json node is not of type String
     */
    private void checkNodeTypeString(JsonNode obj, String attr) throws ServiceException {
        JsonNode node = obj.get(attr);
        try {
            if (!node.getNodeType().equals(JsonNodeType.STRING)) {
                throw new ServiceException(400, "Property " + attr + " in " + Thread.currentThread().getStackTrace()[3].getMethodName() + " has to be a string.");
            }
        }catch(NullPointerException e) {
            //Do nothing. It is legal that some json-properties are null. This will be tested afterwards
        }
    }

    /**
     * Checks if a json node is of type Object
     * @param obj as JsonNode
     * @param attr (property) as String
     * @throws ServiceException if json node is not of type Object
     */
    private void checkNodeTypeObject(JsonNode obj, String attr) throws ServiceException {
        JsonNode node = obj.get(attr);
        try {
            if (!node.getNodeType().equals(JsonNodeType.OBJECT)) {
                throw new ServiceException(400, "Property " + attr + " in " + Thread.currentThread().getStackTrace()[3].getMethodName() + " has to be an object.");
            }
        }catch(NullPointerException e) {
            //Do nothing. It is legal that some json-properties are null. This will be tested afterwards
        }
    }

    /**
     * Checks if a json node is of type Object or Array
     * @param obj as JsonNode
     * @param attr (property) as String
     * @throws ServiceException if json node is not of type Object or Array
     */
    private void checkNodeTypeArrayOrObject(JsonNode obj, String attr) throws ServiceException {
        JsonNode node = obj.get(attr);
        try {
            if (!node.getNodeType().equals(JsonNodeType.ARRAY) && !node.getNodeType().equals(JsonNodeType.OBJECT)) {
                throw new ServiceException(400, "Property " + attr + " in " + Thread.currentThread().getStackTrace()[3].getMethodName() + " has to be an array.");
            }
        }catch(NullPointerException e) {
            //Do nothing. It is legal that some json-properties are null. This will be tested afterwards
        }
    }

    /**
     * checks if a json node is of type Number
     * @param obj as JsonNode
     * @param attr (property) as String
     * @throws ServiceException if json node is not of type Number
     */
    private void checkNodeTypeInteger(JsonNode obj, String attr) throws ServiceException {
        JsonNode node = obj.get(attr);
        try {
            if (!node.getNodeType().equals(JsonNodeType.NUMBER)) {
                throw new ServiceException(400, "Property " + attr + " in " + Thread.currentThread().getStackTrace()[3].getMethodName() + " has to be an integer.");
            }
        }catch(NullPointerException e) {
            //Do nothing. It is legal that some json-properties are null. This will be tested afterwards
        }
    }
}