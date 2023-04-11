package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.repository.FileReader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
class PromptServiceImpl implements PromptService {

    private final FileReader fileReader;

    PromptServiceImpl(FileReader fileReader) {
        this.fileReader = fileReader;
    }

    @Override
    public List<Prompt> getPrompts() {
        File file = new File("src/main/resources/firstDeck.txt");

        try {
            List<Prompt> prompts = new ArrayList<>(fileReader.readFile(file));
            Collections.shuffle(prompts);
            return prompts;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
