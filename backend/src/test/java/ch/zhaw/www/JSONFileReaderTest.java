package ch.zhaw.www;

import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.service.JSONFileReader;
import ch.zhaw.www.service.TXTFileReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JSONFileReaderTest {

    @Test
    void readFile() {
        File testFile = new File("src/test/resources/testDeck.json");
        JSONFileReader jsonFileReader = new JSONFileReader();

        List<Prompt> prompts = new ArrayList(jsonFileReader.readFile(testFile));

        assertEquals(12, prompts.size());
        assertEquals("Ich möchte mal irgendwo leben, wo es kein(e/n) WALTER gibt.", prompts.get(10).getStatement());
        assertEquals(3, prompts.get(11).getTotalPlaceholders());
    }
}