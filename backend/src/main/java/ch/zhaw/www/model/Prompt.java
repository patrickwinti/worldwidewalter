package ch.zhaw.www.model;

/**
 * Prompt Class stores a sentence with a statements that has one or more "placeholder" words to be completed
 * by the players' proposition.
 * The prompt class stores the total number of placeholder in the statement.
 */

public class Prompt {

    private int totalPlaceholders;
    private String  statement;

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
     * Method to count the total number of WALTER (or WALTER related) words that appear in one statement.
     * @param input: sentece to be analysed.
     * @return total number of "WALTER" words.
     */
    public int countPlaceholders(String input) {
        String[] words = input.split("\\s+");
        int count = 0;

        for (String word : words) {
            if (word.equals("WALTER") || word.equals("WALTERN")) {
                count++;
            }
        }

            return count;
        }

}
