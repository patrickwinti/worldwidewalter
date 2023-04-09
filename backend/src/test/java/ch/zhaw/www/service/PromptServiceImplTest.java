package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptServiceImplTest {


    @Test
    void shufflePrompts() {
        PromptServiceImpl promptServiceImpl = new PromptServiceImpl();
        List<Prompt> prompts1 = new ArrayList<>();
        prompts1.add(new Prompt("Wenn du Raubkatzen fürchtest, so nähere dich nie einer Frau WALTER.\n", 1));
        prompts1.add(new Prompt("Wenn meine Geliebte traurige Geschichten hören möchte, so erzähle ich ihr von der Zeit, als ich WALTERTE.", 1));
        prompts1.add(new Prompt("Elektrische Installationen im Haushalt sind für Frauen WALTER.\n", 1));
        prompts1.add(new Prompt("WALTER ist eine Schriftstellerin, die ich männlichen Koryphäen wie Goethe und Schiller bei weitem vorziehe.", 1));
        prompts1.add(new Prompt("WALTER WALTERN WALTER", 3));

        List<Prompt> prompts2 = List.copyOf(prompts1);

        promptServiceImpl.shufflePrompts(prompts1);

        assertNotEquals(prompts2, prompts1);
    }
}