package ch.zhaw.www.repository;

import ch.zhaw.www.model.Prompt;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Public class to read Files of TXT type.
 * Class implements readFile method from the ResourceReader interface.
 */
@Repository
class TextResourceReader implements ResourceReader {
    private static final Pattern PATTERN = Pattern.compile("\\bWALTER(E|T|TEN|TE|N|ST|chen)?\\b");
    private static final Logger LOGGER = Logger.getLogger(TextResourceReader.class.getSimpleName());
    
    /**
     * Reads a file and returns a list of prompts.
     *
     * @param file the file to read
     * @return a list of prompts
     * @throws FileReaderError.WrongFileFormatException if the file has the wrong format
     * @throws IOException                              if an I/O error occurs while reading the file
     */
    @Override
    public List<Prompt> readResource(Resource resource) throws ResourceReaderError.WrongResourceFormatException {
        try (var inputStreamReader = new InputStreamReader(resource.getInputStream());
             var bufferedReader = new BufferedReader(inputStreamReader)) {
            return bufferedReader.lines()
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isEmpty())
                    .map(sentence -> {
                        long totalPlaceholders = countPlaceholders(sentence);
                        if (totalPlaceholders > 0) {
                            return new Prompt(sentence, totalPlaceholders);
                        } else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (ResourceReaderError | IOException e) {
            throw new ResourceReaderError.WrongResourceFormatException();
        }
    }
    
    /**
     * Method to count the total number of WALTER words (or variations) that appear in one statement.
     *
     * @param input the sentence to be analyzed
     * @return the total number of WALTER words
     */
    protected long countPlaceholders(String input) {
        Matcher matcher = PATTERN.matcher(input);
        
        long count = matcher.results().count();
        
        if (count == 0) {
            LOGGER.log(Level.WARNING, "No placeholder match found");
        }
        return count;
    }
}


