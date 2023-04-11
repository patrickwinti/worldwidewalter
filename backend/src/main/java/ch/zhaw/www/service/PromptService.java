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
     * Method to read text file with WALTER prompts
     * @return List with read WALTER prompts
     */
    public List<Prompt> getPrompts();

    }
