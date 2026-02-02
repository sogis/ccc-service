package ch.so.agi.cccservice.message;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.so.agi.cccservice.exception.MessageMalformedException;
import ch.so.agi.cccservice.exception.MessageUnknownException;
import jakarta.validation.ConstraintViolationException;

class MessageTest {

    @Test
    void missingMethodField_throwsMalformed() {
        String json = """
                {
                    "clientName": "TestApp"
                }
                """;

        assertThrows(MessageMalformedException.class, () -> Message.forJsonString(json));
    }

    @Test
    void unknownMethod_throwsUnknown() {
        String json = """
                {
                    "method": "unknownMethod"
                }
                """;

        assertThrows(MessageUnknownException.class, () -> Message.forJsonString(json));
    }

    @Test
    void malformedJson_throwsMalformed() {
        String json = "{ not valid json }";

        assertThrows(MessageMalformedException.class, () -> Message.forJsonString(json));
    }

    @Test
    void missingRequiredField_throwsConstraintViolation() {
        String json = """
                {
                    "method": "connectApp"
                }
                """;

        assertThrows(ConstraintViolationException.class, () -> Message.forJsonString(json));
    }
}
