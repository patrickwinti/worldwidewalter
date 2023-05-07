package ch.zhaw.www.repository;

import ch.zhaw.www.model.Prompt;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * Reads and parses the "WALTER" prompts from a file
 */
public interface ResourceReader {
    
    /**
     * Reads resource and returns a list of Prompts.
     *
     * @param resource input resource to be read
     * @return List with parsed prompts
     * @throws ResourceReaderError.WrongResourceFormatException if the file has the wrong format
     * @throws IOException                              if an I/O error occurs while reading the file
     */
    List<Prompt> readResource(Resource resource) throws ResourceReaderError.WrongResourceFormatException;
}

