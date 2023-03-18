package ch.zhaw.www;

import ch.zhaw.www.model.Prompt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromptTest {

    private Prompt example1 = new Prompt("Vielleicht habe ich in einem früheren Leben schon mal gelebt - bestimmt war ich aber nicht unter den Leuten, die WALTERTEN.");
    private Prompt example2 = new Prompt("Auszug aus dem Parteiprogramm der Grünen in zehn Jahren: \"Wir verlangen, dass WALTER\".");
    private Prompt example3 = new Prompt("Mit meinem Flair für klare Handzeichen kann ich auch als Börsenmakler an der Wall Street arbeiten: Zeige ich dort mit dem kleinen Finger auf einen Kollegen, so will ich damit sagen, dass dieser WALTERE.");
    private Prompt example4 = new Prompt("WALTER ist ein unvorstellbar dummes Lebewesen; es WALTERT.");
    private Prompt example5 = new Prompt("Manchmal verspüre ich den seltsamen Drang, WALTER zu WALTERN.");
    private Prompt example6 = new Prompt("Ein guter Rat: Küss nie ein(e/n) WALTER!");
    private Prompt example7 = new Prompt("Die nächste Pandemie-Panik wird wegen des WALTER-Virus ausbrechen.");
    private Prompt example8 = new Prompt("Mir schwebt ein noch nie gesehener technischer Gag für den nächsten James Bond-Film vor: 007 Walter.");
    private Prompt example9 = new Prompt("WALTER WALTERN WARTERTE WALTERT Walter.");
    private Prompt example10 = new Prompt("Hallo ich heisse WALTER");

    /**
     * Checks that the correct ammount of placeholders has been set.
     */
    @Test
    void getTotalPlaceholders() {

        assertEquals(1, example1.getTotalPlaceholders());
        assertEquals(1, example2.getTotalPlaceholders());
        assertEquals(1, example3.getTotalPlaceholders());
        assertEquals(2, example4.getTotalPlaceholders());
        assertEquals(2, example5.getTotalPlaceholders());
        assertEquals(1, example6.getTotalPlaceholders());
        assertEquals(1, example7.getTotalPlaceholders());
        assertEquals(0, example8.getTotalPlaceholders());
        assertEquals(3, example9.getTotalPlaceholders());
    }

    @Test
    void getStatement() {
        assertEquals("Hallo ich heisse WALTER", example10.getStatement());
    }

    @Test
    void isHasBeenUsed() {
        assertFalse(example3.isHasBeenUsed());
    }

    @Test
    void setHasBeenUsed() {
        example1.setHasBeenUsed(true);

        assertTrue(example1.isHasBeenUsed());
    }
}