package ch.zhaw.www.repository;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Prompt;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
class PromptRepositoryImpl implements PromptRepository {
    
    private final List<Prompt> defaultDeck;
    
    PromptRepositoryImpl(ResourceReader resourceReader, GameProperties gameProperties) {
        defaultDeck = resourceReader.readResource(gameProperties.getDefaultDeck());
    }
    
    @Override
    public List<Prompt> getPrompts() {
        List<Prompt> prompts = new ArrayList<>(defaultDeck);
        Collections.shuffle(prompts);
        return prompts;
    }
    
}
