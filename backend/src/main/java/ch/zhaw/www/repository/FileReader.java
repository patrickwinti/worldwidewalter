package ch.zhaw.www.repository;

import ch.zhaw.www.model.Prompt;

import java.io.File;
import java.util.List;

/**
 * Interface to read and parse the "WALTER" prompts from a file
 */
public interface FileReader {

    /**
     * Method to read file and return a list of Prompts from a JSON file
     *
     * @param file TXT file input
     * @return List with parsed prompts
     */
    List<Prompt> readFile(File file) throws FileReaderError.WrongFileFormatError;
}

