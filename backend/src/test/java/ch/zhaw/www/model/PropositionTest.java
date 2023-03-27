package ch.zhaw.www.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropositionTest {
    
    @Test
    void testNotNullViolation() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        
        Proposition proposition = new Proposition(null, new ArrayList<>(Arrays.asList("Car", "Plane", "boat")));
        Set<ConstraintViolation<Proposition>> violations = validator.validate(proposition);
        
        assertEquals(1, violations.size());
    }
    
    @Test
    void testNoViolation() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Proposition proposition = new Proposition(UUID.randomUUID().toString(), new ArrayList<>(Arrays.asList("Car", "Plane", "boat")));
        Set<ConstraintViolation<Proposition>> violations = validator.validate(proposition);

        assertEquals(0, violations.size());
    }
}