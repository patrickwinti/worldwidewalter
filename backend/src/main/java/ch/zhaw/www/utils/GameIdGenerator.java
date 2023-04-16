package ch.zhaw.www.utils;

/**
 * Service class that generates game IDs
 */
public interface GameIdGenerator {

    /**
     * generates a unique game ID
     * @return a string containing the unique game ID
     */
    String generateId();
}
