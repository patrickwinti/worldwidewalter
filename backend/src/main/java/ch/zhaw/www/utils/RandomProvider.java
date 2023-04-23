package ch.zhaw.www.utils;

/**
 * Class that generates Postfixes for a player name and Ids.
 *
 */
public interface RandomProvider {

    /**
     * Generates a random postfix for a player name.
     *
     * @return random postfix
     */
    int getPostfix();


    /**
     * Generates a unique Id of eight characters
     *
     * @return a string containing the Id
     */
    String getEightCharacterId();
}
