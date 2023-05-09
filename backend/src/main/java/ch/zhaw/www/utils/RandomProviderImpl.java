package ch.zhaw.www.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomProviderImpl implements RandomProvider {
    private static final int SEED = 0;
    private final Random random = new Random(SEED);
    private static final int MIN_GAME_ID_LENGTH = 7;

    private static final char[] CHARS = new char[]{
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    @Override
    public int getPostfix() {
        return random.nextInt(2000);
    }

    @Override
    public String getEightCharacterId() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; id.length() < MIN_GAME_ID_LENGTH; i++) {
            int index = random.nextInt(CHARS.length - i - 1);
            char a = CHARS[i + index];
            CHARS[i + index] = CHARS[i];
            CHARS[i] = a;
            id.append(a);
        }
        return id.toString();
    }
    
}