package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Public class to read Files of TXT type.
 * Class implements readFile method from the FileReader interface.
 */
public class TXTFileReader implements FileReader {

    /**
     * Method to read file and return a list of Prompts from a JSON file
     *
     * @param file TXT file input
     * @return List with parsed prompts
     */
    @Override
    public List<Prompt> readFile (File file) {
        // Path to be hard coded for first iteration.
        List<String> lines;
        List<Prompt> prompts = new ArrayList<>();

        try {
            // Read all lines from the file
            lines = Files.readAllLines(Path.of(file.getPath()));

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

