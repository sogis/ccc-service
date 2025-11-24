package ch.so.agi.cccservice.message.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotTheNullNodeValidator implements ConstraintValidator<NotTheNullNode, JsonNode> {

    @Override
    public boolean isValid(JsonNode value, ConstraintValidatorContext context) {
        // NullNode check AND Java null check
        return value != null && !(value instanceof NullNode);
    }
}

