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
    public List<Prompt> createPromptDeck(File file) throws IOException {
        TextFileReader textFileReader = new TextFileReader();

        List<Prompt> prompts = new ArrayList<>(textFileReader.readFile(file));
        shufflePrompts(prompts);

        return prompts;
    }
}
