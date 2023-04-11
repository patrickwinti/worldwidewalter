package ch.zhaw.www.repository;

import ch.zhaw.www.model.Prompt;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface to read and parse the "WALTER" prompts from a file
 */
public interface FileReader {

    /**
     * Method to read file and return a list of Prompts from a file
     *
     * @param file input file to be read
     * @return List with parsed prompts
     */
    List<Prompt> readFile(File file) throws FileReaderError.WrongFileFormatException, IOException;
}

