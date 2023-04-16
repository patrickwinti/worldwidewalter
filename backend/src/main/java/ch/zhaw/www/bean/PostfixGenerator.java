package ch.zhaw.www.bean;

/**
 * Class that generates a random postfix for a player name.
 */
public interface PostfixGenerator {

    /**
     * Generates a random postfix for a player name in the range of 0 to 1999.
     *
     * @return random postfix
     */
    int getRandomPostfix();
}
