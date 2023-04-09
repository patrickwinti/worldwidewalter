package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.repository.TextFileReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PromptServiceImpl implements PromptService {

    @Override
    public void shufflePrompts(List<Prompt> prompts) {
        Collections.shuffle(prompts);
    }

    @Override
    public List<Prompt> createPromptDeck() throws IOException {
        TextFileReader textFileReader = new TextFileReader();
        File file = new File("backend/src/main/resources/firstDeck.txt");

        List<Prompt> prompts = new ArrayList<>(textFileReader.readFile(file));
        shufflePrompts(prompts);

        return prompts;
    }
}
