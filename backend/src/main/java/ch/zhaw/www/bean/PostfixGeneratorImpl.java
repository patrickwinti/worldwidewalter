package ch.zhaw.www.bean;

import java.util.Random;

public class PostfixGeneratorImpl implements PostfixGenerator {
    private static final int SEED = 0;
    private final Random random = new Random(SEED);

    @Override
    public int getRandomPostfix() {
        return random.nextInt(2000);
    }
}
