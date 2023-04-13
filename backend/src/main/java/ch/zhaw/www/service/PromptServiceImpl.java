package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.repository.ResourceReader;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
class PromptServiceImpl implements PromptService {
    
    private final List<Prompt> defaultDeck;
    
    PromptServiceImpl(ResourceReader resourceReader, GameProperties gameProperties) {
        defaultDeck = resourceReader.readResource(gameProperties.getDefaultDeck());
    }
    
    @Override
    public List<Prompt> getPrompts() {
        List<Prompt> prompts = new ArrayList<>(defaultDeck);
        Collections.shuffle(prompts);
        return prompts;
    }
    
}
