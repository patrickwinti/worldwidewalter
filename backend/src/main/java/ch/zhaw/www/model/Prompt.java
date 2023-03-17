package ch.zhaw.www.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prompt Class stores a sentence with a statements that has one or more "placeholder" words to be completed
 * by the players' proposition.
 * The prompt class stores the total number of placeholder in the statement.
 */

public class Prompt {

    private final int totalPlaceholders;
    private final String  statement;

    public Prompt (String sentence) {
        this.statement = sentence;
        totalPlaceholders = countPlaceholders(sentence);
    }

    public int getTotalPlaceholders () {
        return totalPlaceholders;
    }

    public String getStatement () {
        return statement;
    }

    /**
     * Method to count the total number of WALTER words (or variations) that appear in one statement.
     * @param input: sentece to be analysed.
     * @return total number of "WALTER" words.
     */
    private int countPlaceholders(String input) {
        // Match "WALTER", "WALTERN", "WALTERTE", "WALTERT", or "WALTERTEN"
        Pattern pattern = Pattern.compile("\\bWALTER(TEN|TE|N|ST|chen)?\\b");
        Matcher matcher = pattern.matcher(input);

        int count = 0;
        while (matcher.find()) {
            count++;
        }

        return count;
        }
}
