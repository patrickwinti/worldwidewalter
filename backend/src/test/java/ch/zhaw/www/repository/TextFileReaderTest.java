package ch.zhaw.www.repository;

import ch.zhaw.www.model.Prompt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TextFileReaderTest {
    @TempDir
    private Path tempDir;
    
    @Test
    void readFile() throws IOException {
        File testFile = new File("src/test/resources/testDeck.txt");
        TextFileReader txtFileReader = new TextFileReader();
        
        List<Prompt> prompts = new ArrayList<>(txtFileReader.readFile(testFile));
        
        assertEquals(4, prompts.size());
        assertNotEquals(10, prompts.size());
        assertEquals("WALTER WALTERN WALTER", prompts.get(3).getStatement());
        assertEquals(3, prompts.get(3).getNumberOfPlaceholders());
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
    void testBadInputs() throws IOException {
        Path file = tempDir.resolve("deck.txt");
        Files.write(file, List.of("no placeholder", "Walter", "                    ", ""));
        
        TextFileReader txtFileReader = new TextFileReader();
        List<Prompt> prompts = txtFileReader.readFile(file.toFile());
        
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
        
        TextFileReader txtFileReader = new TextFileReader();
        List<Prompt> prompts = txtFileReader.readFile(file.toFile());
        
        assertEquals(1, prompts.size());
        assertEquals(expectedNumberOfPlaceholders, prompts.get(0).getNumberOfPlaceholders());
    }
    
    @Test
    void testReadFileIOExceptionThrowing() {
        File nonExistingFile = new File("nonExistingFile.txt");
        TextFileReader txtFileReader = new TextFileReader();
        assertThrows(IOException.class, () -> txtFileReader.readFile(nonExistingFile));
    }
    
}