package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Prompt Service class to integrate prompts into the game.
 */
public interface PromptService {
    /**
     * Method to shuffle a list.
     *
     * @param prompts: list of prompts to be shuffled
     */
    void shufflePrompts(List<Prompt> prompts);

    /**
     * Method to create a prompt deck with WALTER prompts.
     *
     */
    List<Prompt> createPromptDeck(File file) throws IOException;
}
