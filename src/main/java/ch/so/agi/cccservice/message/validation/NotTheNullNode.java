package ch.so.agi.cccservice.message.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotTheNullNodeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotTheNullNode {

    String message() default "JsonNode must not be a Jackson NullNode";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

