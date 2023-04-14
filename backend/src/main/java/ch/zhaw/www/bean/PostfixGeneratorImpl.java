package ch.zhaw.www.bean;

import java.util.Random;

public class PostfixGeneratorImpl implements PostfixGenerator {
    private static final int SEED = 0;
    private final Random random = new Random(SEED);
    /**
     * Generates a random postfix for a player name in the range of 0 to 1999.
     *
     * @return random postfix
     */
    @Override
    public int getRandomPostfix() {
        return random.nextInt(2000);
    }
}
