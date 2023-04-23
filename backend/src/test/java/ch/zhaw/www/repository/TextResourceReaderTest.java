package ch.zhaw.www.repository;

import ch.zhaw.www.model.Prompt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest
class TextResourceReaderTest {
    @TempDir
    private Path tempDir;

    @MockBean
    private static Resource resource;

    @Test
    void readResource() throws IOException {
        File testFile = new File("src/test/resources/testDeck.txt");
        TextResourceReader textResourceReader = new TextResourceReader();
        List<Prompt> prompts = new ArrayList<>(textResourceReader.readResource(getResourceFromFile(testFile)));
        
        assertEquals(4, prompts.size());
        assertNotEquals(10, prompts.size());
        assertEquals("WALTER WALTERN WALTER", prompts.get(3).getStatement());
        assertEquals(3, prompts.get(3).getNumberOfPlaceholders());
    }
    
    @Test
    void testBadInputs() throws IOException {
        Path file = tempDir.resolve("deck_2.txt");
        Files.write(file, List.of("no placeholder", "Walter", "                    ", ""));
        
        TextResourceReader txtFileReader = new TextResourceReader();
        Resource resourceFromFile = getResourceFromFile(file.toFile());
        List<Prompt> prompts = txtFileReader.readResource(resourceFromFile);
        
        assertEquals(0, prompts.size());
    }
    
    /**
     * Checks that the correct amount of placeholders has been set.
     */
    @ParameterizedTest
    @MethodSource("provideWalterStatements")
    void testPlaceholderCounting(String input, int expectedNumberOfPlaceholders) throws IOException {
        Path file = tempDir.resolve("deck.txt");
        Files.write(file, List.of(input));
        
        TextResourceReader txtFileReader = new TextResourceReader();
        List<Prompt> prompts = txtFileReader.readResource(getResourceFromFile(file.toFile()));
        
        assertEquals(1, prompts.size());
        assertEquals(expectedNumberOfPlaceholders, prompts.get(0).getNumberOfPlaceholders());
    }
    
    private static Stream<Arguments> provideWalterStatements() {
        return Stream.of(
                Arguments.of("Vielleicht habe ich in einem früheren Leben schon mal gelebt - bestimmt war ich aber nicht unter den Leuten, die WALTERTEN.", 1),
                Arguments.of("Auszug aus dem Parteiprogramm der Grünen in zehn Jahren: \"Wir verlangen, dass WALTER\".", 1),
                Arguments.of("Viele Männer reden ihre Geliebten mit \"Mäuschen\" oder \"Spätzchen\" an. Ich finde das beleidigend. Wenn schon, so möchte ich bitteschön mit \"WALTERchen\" angeredet werden!", 1),
                Arguments.of("WALTER ist ein unvorstellbar dummes Lebewesen; es WALTERT.", 2),
                Arguments.of("Manchmal verspüre ich den seltsamen Drang, WALTER zu WALTERN.", 2),
                Arguments.of("Ein guter Rat: Küss nie ein(e/n) WALTER!", 1),
                Arguments.of("Die nächste Pandemie-Panik wird wegen des WALTER-Virus ausbrechen.", 1),
                Arguments.of("WALTER WALTERN WARTERTE WALTERT Walter.", 3),
                Arguments.of("Hallo ich heisse WALTER", 1)
        );
    }
    
    @Test
    void testReadFileIOExceptionThrowing() throws IOException {
        doThrow(FileNotFoundException.class).when(resource).getInputStream();
        
        TextResourceReader txtFileReader = new TextResourceReader();
        assertThrows(ResourceReaderError.WrongResourceFormatException.class, () -> txtFileReader.readResource(resource));
    }
    
    private static Resource getResourceFromFile(File file) throws IOException {
        when(resource.getInputStream()).thenReturn(new FileInputStream(file));
        return resource;
    }
}