package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Public class to read Files of TXT type.
 * Class implements readFile method from the FileReader interface.
 */
public class TXTFileReader implements FileReader {

    /**
     * Method to read file and return a list of Prompts
     * @param filePath Path to the file to be read
     * @return List with Prompts
     */
    @Override
    public List<Prompt> readFile(Path filePath) {
        // Path to be hard coded for first iteration.
        filePath = Paths.get(filePath.toUri());

        List<String> lines;
        List<Prompt> prompts = new ArrayList<>();

        try {
            // Read all lines from the file
            lines = Files.readAllLines(filePath);

            // Print out each line
            for (String line : lines) {
                prompts.add(new Prompt(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return prompts;
    }
}

