package ch.zhaw.www.repository;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Prompt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.io.Resource;

import java.util.List;

import static ch.zhaw.www.TestHelper.WALTER_MARKER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
class PromptRepositoryTest {

    @MockitoBean
    GameProperties gameProperties;

    @MockitoBean
    ResourceReader resourceReader;

    @MockitoBean
    Resource resource;

    @Test
    void getPrompts() {
        List<Prompt> originalList = List.of(new Prompt(WALTER_MARKER + " " + WALTER_MARKER + "WALTEROO", List.of("WALTER, WALTER")),
                                            new Prompt(WALTER_MARKER + " " + WALTER_MARKER + " hello", List.of("WALTER, WALTER")),
                                            new Prompt(WALTER_MARKER + " says hi", List.of("WALTER")));

        when(resourceReader.readResource(any())).thenReturn(originalList);

        PromptRepository promptRepository = new PromptRepositoryImpl(resourceReader, gameProperties);
        List<Prompt> prompts = promptRepository.getPrompts();

        assertTrue(prompts.containsAll(originalList));
        assertEquals(prompts.size(), originalList.size());
    }

    @Test
    void getPromptsException() {

        doThrow(ResourceReaderError.WrongResourceFormatException.class).when(resourceReader).readResource(any());

        assertThrows(ResourceReaderError.WrongResourceFormatException.class, ()-> new PromptRepositoryImpl(resourceReader, gameProperties));
    }

}