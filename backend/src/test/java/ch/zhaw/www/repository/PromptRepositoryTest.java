package ch.zhaw.www.repository;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Prompt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class PromptRepositoryTest {

    @MockBean
    GameProperties gameProperties;

    @MockBean
    ResourceReader resourceReader;

    @MockBean
    Resource resource;

    @Test
    void getPrompts() {
        List<Prompt> originalList = List.of(new Prompt("WALTER WALTER WALTEROO", 2),
                                            new Prompt("WALTER WALTER hello", 1),
                                            new Prompt("WALTER says hi", 1));

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