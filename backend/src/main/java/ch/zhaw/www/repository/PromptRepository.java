package ch.zhaw.www.repository;

import ch.zhaw.www.model.Prompt;

import java.util.List;

/**
 * Prompt repository to integrate prompts into the game.
 */
public interface PromptRepository {

    /**
     * Returns a list of Prompt objects
     *
     * @return List of prompts
     */
    List<Prompt> getPrompts();
}
