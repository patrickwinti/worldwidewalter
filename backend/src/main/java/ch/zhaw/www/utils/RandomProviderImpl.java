package ch.zhaw.www.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

@Component
public class RandomProviderImpl implements RandomProvider {
    private static final int ROOM_CODE_LENGTH = 4;

    /**
     * Uppercase letters only, excluding the vowels A/E/I/O/U so that random codes cannot
     * spell real (potentially offensive) words, and excluding the ambiguous I/O. The
     * remaining consonants are unambiguous when read aloud or typed.
     */
    private static final char[] ROOM_CODE_ALPHABET =
            "BCDFGHJKLMNPQRSTVWXYZ".toCharArray();

    private final RandomGenerator random = new SecureRandom();

    @Override
    public int getRandomIndex(int bound) {
        return random.nextInt(bound);
    }

    @Override
    public String getRoomCode() {
        StringBuilder code = new StringBuilder(ROOM_CODE_LENGTH);
        for (int i = 0; i < ROOM_CODE_LENGTH; i++) {
            code.append(ROOM_CODE_ALPHABET[random.nextInt(ROOM_CODE_ALPHABET.length)]);
        }
        return code.toString();
    }

}