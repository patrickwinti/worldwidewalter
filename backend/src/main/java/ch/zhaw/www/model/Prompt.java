package ch.zhaw.www.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prompt Class stores a statement that has one or more "placeholder" words. These placeholders are to be completed
 * by the players' proposition.
 * The prompt stores the total number of placeholder in the statement,
 * and tracks whether it has been used before in the game.
 */

public class Prompt {

    private final int totalPlaceholders;
    private final String statement;
    private boolean hasBeenUsed;

    public Prompt (String sentence) {
        this.statement = sentence;
        totalPlaceholders = countPlaceholders(sentence);
        hasBeenUsed = false;
    }

    public int getTotalPlaceholders () {
        return totalPlaceholders;
    }

    public String getStatement () {
        return statement;
    }

    public boolean isHasBeenUsed () {
        return hasBeenUsed;
    }

    public void setHasBeenUsed (boolean hasBeenUsed) {
        this.hasBeenUsed = hasBeenUsed;
    }

    /*
     * Method to count the total number of WALTER words (or variations) that appear in one statement.
     *
     * @param input: sentence to be analysed.
     * @return total number of "WALTER" words.
     */
    private int countPlaceholders (String input) {
        Pattern pattern = Pattern.compile("\\bWALTER(E|T|TEN|TE|N|ST|chen)?\\b");
        Matcher matcher = pattern.matcher(input);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
    }
}
