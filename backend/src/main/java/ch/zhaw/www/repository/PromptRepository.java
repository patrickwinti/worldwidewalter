package ch.zhaw.www.repository;

import ch.zhaw.www.model.Prompt;

import java.util.List;

/**
 * Prompt Service class to integrate prompts into the game.
 */
public interface PromptRepository {

    /**
     * Method to read text file with WALTER prompts
     *
     * @return List with read WALTER prompts
     */
    List<Prompt> getPrompts();
}
