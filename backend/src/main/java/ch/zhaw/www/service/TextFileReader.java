package ch.zhaw.www.service;

import ch.zhaw.www.controller.GameController;
import ch.zhaw.www.model.Prompt;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Public class to read Files of TXT type.
 * Class implements readFile method from the FileReader interface.
 */
public class TextFileReader implements FileReader {

    private final Logger logger = Logger.getLogger(GameController.class.getSimpleName());

    /**
     * Method to read file and return a list of Prompts from a JSON file
     *
     * @param file TXT file input
     * @return List with parsed prompts
     */

    public List<Prompt> readFile(File file) {
        List<Prompt> prompts = new ArrayList<>();
        try {
            Files.lines(file.toPath())
                    .filter(line -> !line.trim().isEmpty())
                    .forEach(line -> {
                        try {
                            prompts.add(new Prompt(line));
                        } catch (Exception e) {
                            logger.log(Level.WARNING, e.getMessage());
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return prompts;
    }
}

