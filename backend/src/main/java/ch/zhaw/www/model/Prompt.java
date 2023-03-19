package ch.zhaw.www.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * Prompt Class stores a statement that has one or more "placeholder" words. These placeholders are to be completed
 * by the players' proposition.
 * The prompt stores the total number of placeholder in the statement,
 * and tracks whether it has been used before in the game.
 */
@Data
public class Prompt {

    private final int totalPlaceholders;
    private final String statement;
    private boolean hasBeenUsed;

    public Prompt (String sentence) {
        this.statement = sentence;
        totalPlaceholders = countPlaceholders(sentence);
        hasBeenUsed = false;
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

        long count = matcher.results().count();

        // Todo: Handle case when 0 matches

        return (int) count;
    }
}
