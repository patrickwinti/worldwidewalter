package ch.zhaw.www;

import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.service.TextFileReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextFileReaderTest {

    @Test
    void readFile() {
        File testFile = new File("src/test/resources/testDeck.txt");
        TextFileReader txtFileReader = new TextFileReader();

        List<Prompt> prompts = null;
        try {
            prompts = new ArrayList<>(txtFileReader.readFile(testFile));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertEquals(4, prompts.size());
        assertNotEquals(10, prompts.size());
        assertEquals("WALTER WALTERN WALTER", prompts.get(3).getStatement());
        assertEquals(3, prompts.get(3).getTotalPlaceholders());
    }
}