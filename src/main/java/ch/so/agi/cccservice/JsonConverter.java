package ch.so.agi.cccservice;

import ch.so.agi.cccservice.messages.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Transforms a ccc message to JSON and vice versa.
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
     * @throws IOException if the message can not be converted
     * @throws ServiceException  if the message can not be converted
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

        checkNodeTypes(obj);

        try {
             if (method.equals(ConnectAppMessage.METHOD_NAME)) {
                 return createConnectAppMessage(obj);
             }
             if (method.equals(ConnectGisMessage.METHOD_NAME)) {
                 return createConnectGisMessage(obj);
             }
             if (method.equals(NotifySessionReadyMessage.METHOD_NAME)) {
                 return createNotifySessionReadyMessage(obj);
             }
             if (method.equals(CreateGeoObjectMessage.METHOD_NAME)) {
                 return createCreateGeoObjectMessage(obj);
             }
             if (method.equals(EditGeoObjectMessage.METHOD_NAME)) {
                 return createEditGeoObjectMessage(obj);
             }
             if (method.equals(ShowGeoObjectMessage.METHOD_NAME)) {
                 return createShowGeoObjectMessage(obj);
             }
             if (method.equals(CancelEditGeoObjectMessage.METHOD_NAME)) {
                 return createCancelEditGeoObjectMessage(obj);
             }
             if (method.equals(NotifyEditGeoObjectDoneMessage.METHOD_NAME)) {
                 return createNotifyEditGeoObjectDoneMessage(obj);
             }
             if (method.equals(NotifyObjectUpdatedMessage.METHOD_NAME)) {
                 return createNotifyObjectUpdatedMessage(obj);
             }
             if (method.equals(NotifyErrorMessage.METHOD_NAME)){
                 return createNotifyErrorMessage(obj);
             }
             if (method.equals(NotifyGeoObjectSelectedMessage.METHOD_NAME)) {
                 return createNotifyGeoObjectSelectedMessage(obj);
             } else {
                 throw new IOException("No suitable methode found in given JSON. Given method: "+obj.get("method").asText());
             }
         } catch (NullPointerException e) {
             throw new ServiceException(400, "One or more arguments missing for given method!");
         }
    }

    /**
     * Checks if all properties of jsonNode are of correct type
     * @param obj jsonNode
     * @throws ServiceException on wrong type
     */
    private void checkNodeTypes(JsonNode obj) throws ServiceException {
        checkNodeTypeString(obj, "method");
        checkNodeTypeString(obj, "apiVersion" );
        checkNodeTypeString(obj, "clientName" );
        checkNodeTypeString(obj, "session" );
        checkNodeTypeObject(obj, "context");
        checkNodeTypeObject(obj, "zoomTo" );
        checkNodeTypeObject(obj, "data" );
        checkNodeTypeArray(obj, "properties" );
        checkNodeTypeArray(obj, "context_list" );
        checkNodeTypeInteger(obj, "code" );
        checkNodeTypeString(obj, "message" );
        checkNodeTypeObject(obj, "userData" );
        checkNodeTypeString(obj, "nativeCode" );
        checkNodeTypeString(obj, "technicalDetails" );
    }

    /**
     * Checks if a property of json node is of type String
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
     * Checks if a property of  json node is of type Object
     * @param obj as JsonNode
     * @param attr (property) as String
     * @throws ServiceException if json node is not of type Object
     */
    private void checkNodeTypeObject(JsonNode obj, String attr) throws ServiceException {
        JsonNode node = obj.get(attr);
        try {
            JsonNodeType nodeType = node.getNodeType();
            if (nodeType.equals(JsonNodeType.NULL)) {
            }else if (nodeType.equals(JsonNodeType.OBJECT)) {
            }else {
                throw new ServiceException(400, "Property " + attr + " in " + Thread.currentThread().getStackTrace()[3].getMethodName() + " has to be an object.");
            }
        }catch(NullPointerException e) {
            //Do nothing. It is legal that some json-properties are null. This will be tested afterwards
        }
    }

    /**
     * Checks if a property of json node is of type Object or Array
     * @param obj as JsonNode
     * @param attr (property) as String
     * @throws ServiceException if json node is not of type Object or Array
     */
    private void checkNodeTypeArrayOrObject(JsonNode obj, String attr) throws ServiceException {
        JsonNode node = obj.get(attr);
        try {
            if (!node.getNodeType().equals(JsonNodeType.ARRAY) && !node.getNodeType().equals(JsonNodeType.OBJECT)) {
                throw new ServiceException(400, "Property " + attr + " in " + Thread.currentThread().getStackTrace()[3].getMethodName() + " has to be an array or object.");
            }
        }catch(NullPointerException e) {
            //Do nothing. It is legal that some json-properties are null. This will be tested afterwards
        }
    }
    /**
     * Checks if a property of json node is of type Array
     * @param obj as JsonNode
     * @param attr (property) as String
     * @throws ServiceException if json node is not of type Array
     */
    private void checkNodeTypeArray(JsonNode obj, String attr) throws ServiceException {
        JsonNode node = obj.get(attr);
        if (node!=null && !node.getNodeType().equals(JsonNodeType.NULL) && !node.getNodeType().equals(JsonNodeType.ARRAY)) {
            throw new ServiceException(400, "Property " + attr + " in " + Thread.currentThread().getStackTrace()[3].getMethodName() + " has to be an array.");
        }
    }

    /**
     * checks if a property of json node is of type Number
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

    /**
     * Generates ConnectAppMessage
     * @param obj JsonNode with ConnectAppMessage-Properties
     * @return ConnectAppMessage
     * @throws ServiceException on missing or wrong properties
     */
    private ConnectAppMessage createConnectAppMessage(JsonNode obj) throws ServiceException {
        ConnectAppMessage appConnectMessage = new ConnectAppMessage();
        appConnectMessage.setApiVersion(obj.get("apiVersion").asText());
        appConnectMessage.setClientName(obj.get("clientName").asText());
        final String sessionIdTxt = obj.get("session").asText();
        if(!SessionId.isValidUuid(sessionIdTxt)) {
            throw new ServiceException(400, "Attribute session in "+ConnectAppMessage.METHOD_NAME+" is invalid.");
        }
        appConnectMessage.setSession(new SessionId(sessionIdTxt));
        if (appConnectMessage.getSession() == null || appConnectMessage.getApiVersion() == null
                || appConnectMessage.getClientName() == null){
            throw new ServiceException(400, "Attribute in "+ConnectAppMessage.METHOD_NAME+" is missing or wrong.");
        }
        return appConnectMessage;
    }

    /**
     * Generates ConnectGisMessage
     * @param obj JsonNode with ConnectGisMessage-Properties
     * @return ConnectGisMessage
     * @throws ServiceException on missing or wrong properties
     */
    private ConnectGisMessage createConnectGisMessage (JsonNode obj) throws ServiceException {
        ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
        gisConnectMessage.setApiVersion(obj.get("apiVersion").asText());
        gisConnectMessage.setClientName(obj.get("clientName").asText());
        final String sessionIdTxt = obj.get("session").asText();
        if(!SessionId.isValidUuid(sessionIdTxt)) {
            throw new ServiceException(400, "Attribute session in "+ConnectGisMessage.METHOD_NAME+" is invalid.");
        }
        gisConnectMessage.setSession(new SessionId(sessionIdTxt));
        if (gisConnectMessage.getSession() == null || gisConnectMessage.getApiVersion() == null ||
                gisConnectMessage.getClientName() == null){
            throw new ServiceException(400, "Attribute in gisConnect is missing or wrong.");
        }
        return gisConnectMessage;
    }

    /**
     * Generates NotifySessionReadyMessage
     * @param obj JsonNode with NotifySessionReadyMessage-Properties
     * @return NotifySessionReadyMessage
     * @throws ServiceException on missing or wrong properties
     */
    private NotifySessionReadyMessage createNotifySessionReadyMessage (JsonNode obj) throws ServiceException {
        NotifySessionReadyMessage readyMessage = new NotifySessionReadyMessage();
        readyMessage.setApiVersion(obj.get("apiVersion").asText());
        if (readyMessage.getApiVersion() == null){
            throw new ServiceException(400, "Attribute apiVersion is missing or wrong.");
        }
        return readyMessage;
    }

    /**
     * Generates CreateGeoObjectMessage
     * @param obj JsonNode with CreateGeoObjectMessage-Properties
     * @return CreateGeoObjectMessage
     * @throws ServiceException on missing or wrong properties
     */
    private CreateGeoObjectMessage createCreateGeoObjectMessage (JsonNode obj) throws ServiceException {
        CreateGeoObjectMessage createMessage = new CreateGeoObjectMessage();
        createMessage.setContext(obj.get("context"));
        createMessage.setZoomTo(obj.get("zoomTo"));
        if (createMessage.getContext() == null) {
            throw new ServiceException(400, "Attribut context is missing or wrong.");
        }
        return createMessage;
    }

    /**
     * Generates EditGeoObjectMessage
     * @param obj JsonNode with EditGeoObjectMessage-Properties
     * @return EditGeoObjectMessage
     * @throws ServiceException on missing or wrong properties
     */
    private EditGeoObjectMessage createEditGeoObjectMessage (JsonNode obj) throws ServiceException {
        EditGeoObjectMessage editMessage = new EditGeoObjectMessage();
        editMessage.setContext(obj.get("context"));
        editMessage.setData(obj.get("data"));
        if (editMessage.getContext() == null || editMessage.getData() == null){
            throw new ServiceException(400, "Attribute in editMessage is missing or wrong.");
        }
        return editMessage;
    }

    /**
     * Generates ShowGeoObjectMessage
     * @param obj JsonNode with ShowGeoObjectMessage-Properties
     * @return ShowGeoObjectMessage
     * @throws ServiceException on missing or wrong properties
     */
    private ShowGeoObjectMessage createShowGeoObjectMessage (JsonNode obj) throws ServiceException {
        ShowGeoObjectMessage showMessage = new ShowGeoObjectMessage();
        showMessage.setContext(obj.get("context"));
        showMessage.setData(obj.get("data"));
        if (showMessage.getContext() == null || showMessage.getData() == null){
            throw new ServiceException(400, "Attribute in showMessage is missing or wrong.");
        }
        return showMessage;
    }

    /**
     * Generates CancelEditGeoObjectMessage
     * @param obj JsonNode with CancelEditGeoObjectMessage-Properties
     * @return CancelEditGeoObjectMessage
     * @throws ServiceException on missing or wrong properties
     */
    private CancelEditGeoObjectMessage createCancelEditGeoObjectMessage (JsonNode obj) throws ServiceException {
        CancelEditGeoObjectMessage cancelMessage = new CancelEditGeoObjectMessage();
        cancelMessage.setContext(obj.get("context"));
        if (cancelMessage.getContext() == null){
            throw new ServiceException(400, "Attribute context is missing or wrong.");
        }
        return cancelMessage;
    }

    /**
     * Generates NotifyEditGeoObjectDoneMessage
     * @param obj JsonNode with NotifyEditGeoObjectDoneMessage-Properties
     * @return NotifyEditGeoObjectDoneMessage
     * @throws ServiceException on missing or wrong properties
     */
    private NotifyEditGeoObjectDoneMessage createNotifyEditGeoObjectDoneMessage (JsonNode obj) throws ServiceException {
        NotifyEditGeoObjectDoneMessage changedMessage = new NotifyEditGeoObjectDoneMessage();
        changedMessage.setContext(obj.get("context"));
        changedMessage.setData(obj.get("data"));
        if (changedMessage.getContext() == null){
            throw new ServiceException(400, "Attribute context is missing or wrong.");
        }
        return changedMessage;
    }

    /**
     * Generates NotifyObjectUpdatedMessage
     * @param obj JsonNode with NotifyObjectUpdatedMessage-Properties
     * @return NotifyObjectUpdatedMessage
     * @throws ServiceException on missing or wrong properties
     */
    private NotifyObjectUpdatedMessage createNotifyObjectUpdatedMessage (JsonNode obj) throws ServiceException {
        NotifyObjectUpdatedMessage dataWrittenMessage = new NotifyObjectUpdatedMessage();
        dataWrittenMessage.setProperties(obj.get("properties"));
        if (dataWrittenMessage.getProperties() == null){
            throw new ServiceException(400, "Attribute properties is missing or wrong.");
        }
        return dataWrittenMessage;
    }

    /**
     * Generates NotifyErrorMessage
     * @param obj JsonNode with NotifyErrorMessage-Properties
     * @return NotifyErrorMessage
     * @throws ServiceException on missing or wrong properties
     */
    private NotifyErrorMessage createNotifyErrorMessage (JsonNode obj) throws ServiceException {

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

    /**
     * Generates NotifyGeoObjectSelectedMessage
     * @param obj JsonNode with NotifyGeoObjectSelectedMessage-Properties
     * @return NotifyGeoObjectSelectedMessage
     */
    private NotifyGeoObjectSelectedMessage createNotifyGeoObjectSelectedMessage (JsonNode obj) {
        NotifyGeoObjectSelectedMessage selectedMessage = new NotifyGeoObjectSelectedMessage();
        selectedMessage.setContext_list(obj.get("context_list"));
        return selectedMessage;
    }
}