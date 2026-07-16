package ch.zhaw.www.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RandomProviderImplementationTest {

    private static final String ALLOWED_ROOM_CODE_CHARS = "BCDFGHJKLMNPQRSTVWXYZ";
    private static final int EXPECTED_ROOM_CODE_LENGTH = 4;

    @Autowired
    private RandomProvider randomProvider;

    @Test
    void testRoomCodeFormat() {
        IntStream.range(0, 10_000).forEach(operand -> {
            var code = randomProvider.getRoomCode();
            assertEquals(EXPECTED_ROOM_CODE_LENGTH, code.length());
            for (char c : code.toCharArray()) {
                assertTrue(ALLOWED_ROOM_CODE_CHARS.indexOf(c) >= 0,
                        () -> "Room code contains disallowed character: " + code);
            }
        });
    }

    @Test
    void testRoomCodesAreNotConstant() {
        var distinctCodes = IntStream.range(0, 1_000)
                .mapToObj(operand -> randomProvider.getRoomCode())
                .distinct()
                .count();
        // Over 1000 draws from a ~194k keyspace we expect almost all to be distinct.
        assertTrue(distinctCodes > 900, "Room codes should be well distributed, got " + distinctCodes);
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
