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
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Public class to read Files of TXT type. Class implements readFile method from the ResourceReader interface.
 */
@Repository
class TextResourceReader implements ResourceReader {
    private static final Pattern PATTERN = Pattern.compile("\\bWALTER(E|T|TEN|TE|N|ST|chen)?\\b");
    private static final Logger LOGGER = Logger.getLogger(TextResourceReader.class.getSimpleName());
    private static final String MARKER = "<<walter>>";
    
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
                            return new Prompt(
                                    replaceWaltersWithMarker(sentence),
                                    totalPlaceholders,
                                    getWalters(sentence));
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
     * Method to replace the WALTER words (or variations) with the marker.
     *
     * @param sentence the sentence with WALTER words
     * @return the sentence with the marker instead of the WALTER words
     */
    protected static String replaceWaltersWithMarker(String sentence) {
        return PATTERN.matcher(sentence).replaceAll(MARKER);
    }
    
    /**
     * Method to count the total number of WALTER words (or variations) that appear in one statement.
     *
     * @param input the sentence to be analyzed
     * @return the total number of WALTER words
     */
    private static long countPlaceholders(String input) {
        Matcher matcher = PATTERN.matcher(input);
        
        long count = matcher.results().count();
        
        if (count == 0) {
            LOGGER.log(Level.WARNING, "No placeholder match found");
        }
        return count;
    }
    
    
    /**
     * Method to get the WALTER words (or variations).
     *
     * @param input the sentence with WALTER words
     * @return the list of WALTER words
     */
    private static List<String> getWalters(String input) {
        return PATTERN.matcher(input).results().map(MatchResult::group).toList();
    }
}


