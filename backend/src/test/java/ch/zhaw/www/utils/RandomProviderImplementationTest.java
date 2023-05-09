package ch.zhaw.www.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RandomProviderImplementationTest {
    
    @Autowired
    private RandomProvider randomProvider;
    
    @Test
    void testUniqueness() {
        final int generatedIds = 10_000_000;
        var distinctIds = IntStream.range(0, generatedIds)
                .mapToObj(operand -> randomProvider.getEightCharacterId())
                .distinct()
                .count();
        double expectedUniquenessPercent = 99.9975 / 100;
        
        assertTrue(distinctIds >= expectedUniquenessPercent * generatedIds);
    }
    
    @Test
    void testPostfixGeneration() {
        var postfix1 = randomProvider.getPostfix();
        var postfix2 = randomProvider.getPostfix();
        
        assertNotEquals(postfix1, postfix2);
        assertTrue(postfix1 >= 0);
        assertTrue(postfix2 >= 0);
        assertTrue(postfix1 < 2000);
        assertTrue(postfix2 < 2000);
    }
}
