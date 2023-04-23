package ch.zhaw.www.bean;

import ch.zhaw.www.model.Proposition;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropositionTest {
    
    @Test
    void testNotNullViolation() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        
        Proposition proposition = new Proposition(null, List.of("Car", "Plane", "boat"));
        Set<ConstraintViolation<Proposition>> violations = validator.validate(proposition);
        
        assertEquals(1, violations.size());
    }
    
    @Test
    void testNoViolation() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        
        Proposition proposition = new Proposition(UUID.randomUUID().toString(), List.of("Car", "Plane", "boat"));
        Set<ConstraintViolation<Proposition>> violations = validator.validate(proposition);
        
        assertEquals(0, violations.size());
    }
}