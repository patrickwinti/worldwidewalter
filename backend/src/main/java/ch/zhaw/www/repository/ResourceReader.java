package ch.zhaw.www.repository;

import ch.zhaw.www.model.Prompt;
import org.springframework.core.io.Resource;

import java.util.List;

/**
 * Interface to read and parse the "WALTER" prompts from a file
 */
public interface ResourceReader {
    
    /**
     * Method to read file and return a list of Prompts from a file
     *
     * @param resource input file to be read
     * @return List with parsed prompts
     */
    List<Prompt> readResource(Resource resource) throws ResourceReaderError.WrongResourceFormatException;
}

