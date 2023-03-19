package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Public class to read Files of TXT type.
 * Class implements readFile method from the FileReader interface.
 */
public class TextFileReader implements FileReader {

    /**
     * Method to read file and return a list of Prompts from a JSON file
     *
     * @param file TXT file input
     * @return List with parsed prompts
     */
    @Override
    public List<Prompt> readFile(File file) {
        List<Prompt> prompts = new ArrayList<>();
        try {
            Files.lines(file.toPath())
                    .filter(line -> !line.trim().isEmpty())
                    .forEach(line -> {
                        try {
                            prompts.add(new Prompt(line));
                        } catch (Exception e) {
                            System.err.println("Error processing line: " + line + ". No WALTER placeholders found");
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return prompts;
    }
}

