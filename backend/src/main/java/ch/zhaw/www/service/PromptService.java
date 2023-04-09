package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;

import java.util.Collections;
import java.util.List;


/**
 * Prompt Service class to integrate prompts into the game.
 */
public class PromptService {

    /**
     * Method to shuffle a list.
     *
     * @param prompts: list of prompts to be shuffled
     */
    public static void shufflePrompts(List<Prompt> prompts) {
        Collections.shuffle(prompts);
    }
}
