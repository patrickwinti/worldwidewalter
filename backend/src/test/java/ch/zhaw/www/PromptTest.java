package ch.zhaw.www;

import ch.zhaw.www.model.Prompt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromptTest {

    // Creating several prompts with different variations of WALTER words
    private final Prompt example1 = new Prompt("Vielleicht habe ich in einem früheren Leben schon mal gelebt - bestimmt war ich aber nicht unter den Leuten, die WALTERTEN.");
    private final Prompt example2 = new Prompt("Auszug aus dem Parteiprogramm der Grünen in zehn Jahren: \"Wir verlangen, dass WALTER\".");
    private final Prompt example3 = new Prompt("Viele Männer reden ihre Geliebten mit \"Mäuschen\" oder \"Spätchen\" an. Ich finde das beleidigend. Wenn schon, so möchte ich bitteschön mit \"WALTERchen\" angeredet werden!");
    private final Prompt example4 = new Prompt("WALTER ist ein unvorstellbar dummes Lebewesen; es WALTERT.");
    private final Prompt example5 = new Prompt("Manchmal verspüre ich den seltsamen Drang, WALTER zu WALTERN.");
    private final Prompt example6 = new Prompt("Ein guter Rat: Küss nie ein(e/n) WALTER!");
    private final Prompt example7 = new Prompt("Die nächste Pandemie-Panik wird wegen des WALTER-Virus ausbrechen.");
    private final Prompt example8 = new Prompt("WALTER WALTERN WARTERTE WALTERT Walter.");
    private final Prompt example9 = new Prompt("Hallo ich heisse WALTER");

    /**
     * Checks that the correct ammount of placeholders has been set.
     */
    @Test
    void testGetTotalPlaceholders() {

        assertEquals(1, example1.getTotalPlaceholders());
        assertEquals(1, example2.getTotalPlaceholders());
        assertEquals(1, example3.getTotalPlaceholders());
        assertEquals(2, example4.getTotalPlaceholders());
        assertEquals(2, example5.getTotalPlaceholders());
        assertEquals(1, example6.getTotalPlaceholders());
        assertEquals(1, example7.getTotalPlaceholders());
        assertEquals(3, example8.getTotalPlaceholders());
    }

    @Test
    void testGetStatement() {
        assertEquals("Hallo ich heisse WALTER", example9.getStatement());
    }

    @Test
    void testHasBeenUsed() {
        assertFalse(example3.isHasBeenUsed());
    }

    @Test
    void testUpdateHasBeenUsed() {
        example1.setHasBeenUsed(true);
        assertTrue(example1.isHasBeenUsed());
    }
}