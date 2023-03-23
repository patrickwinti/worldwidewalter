package ch.zhaw.www.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PromptTest {

    @Test
    void testNotNullViolation() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Prompt prompt = new Prompt(null, 0);
        Set<ConstraintViolation<Prompt>> violations = validator.validate(prompt);

        assertEquals(1, violations.size());
    }

    @Test
    void testNoViolation() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Prompt prompt = new Prompt("Mein Name ist WALTER", 1);
        Set<ConstraintViolation<Prompt>> violations = validator.validate(prompt);

        assertEquals(0, violations.size());
    }
}