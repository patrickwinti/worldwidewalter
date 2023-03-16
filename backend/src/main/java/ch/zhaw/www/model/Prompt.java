package ch.zhaw.www.model;

/**
 * Prompt Class stores a sentence with a statements that has one or more "placeholder" words to be completed
 * by the players' proposition.
 * The prompt class stores the total number of placeholder in the statement.
 */

public class Prompt {

    private int totalPlaceholders;
    private String  statement;

    public Prompt (int totalPlaceholders, String statement) {
        this.totalPlaceholders = totalPlaceholders;
        this.statement = statement;
    }

    public int getTotalPlaceholders () {
        return totalPlaceholders;
    }

    public String getStatement () {
        return statement;
    }


}
