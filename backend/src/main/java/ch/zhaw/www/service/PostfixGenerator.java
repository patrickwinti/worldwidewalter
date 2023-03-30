package ch.zhaw.www.service;

import java.util.Random;

/**
 * Class that generates a random postfix for a player name.
 */
public class PostfixGenerator {
    Random random = new Random(0);

    /**
     * Generates a random postfix for a player name in the range of 0 to 1999.
     *
     * @return random postfix
     */
    public int getRandomPostfix() {
        return random.nextInt(2000);
    }
}
