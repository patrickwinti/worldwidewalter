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
     * Returns a random index in the range {@code [0, bound)}. Used to pick a random player,
     * e.g. when choosing a replacement host.
     *
     * @param bound upper bound (exclusive), must be positive
     * @return a random index between 0 (inclusive) and {@code bound} (exclusive)
     */
    int getRandomIndex(int bound);

    /**
     * Generates a short, human-friendly game room code (e.g. {@code WXYZ}).
     * <p>
     * The code uses only unambiguous uppercase letters so it is easy to read aloud
     * and type. Codes are not guaranteed to be unique on their own; callers must
     * handle collisions (see {@code GameService#createGame}).
     *
     * @return a room code string
     */
    String getRoomCode();
}
