package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;

import java.util.Collections;
import java.util.List;

public class PromptServiceImpl implements PromptService {

    @Override
    public void shufflePrompts(List<Prompt> prompts) {
        Collections.shuffle(prompts);
    }
}
