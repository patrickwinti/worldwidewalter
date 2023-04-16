package ch.zhaw.www.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EightAlphanumericGameIdGeneratorImplTest {
    @Autowired
    private GameIdGenerator gameIdGenerator;
    
    @Test
    void testUniqueness() {
        final int generatedIds = 10_000_000;
        var distinctIds = IntStream.range(0, generatedIds)
                .mapToObj(operand -> gameIdGenerator.generateId())
                .distinct()
                .count();
        double expectedUniquenessPercent = 99.9975 / 100;
        
        assertTrue(distinctIds >= expectedUniquenessPercent * generatedIds);
    }
}