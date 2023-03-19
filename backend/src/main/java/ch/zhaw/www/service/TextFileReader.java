package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    public List<Prompt> readFile (File file) {
        try {
            return Files.readAllLines(Path.of(file.getPath()))
                        .stream()
                        .filter(line -> !line.trim().isEmpty())
                        .map(Prompt::new)
                        .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

