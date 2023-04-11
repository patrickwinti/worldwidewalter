package ch.zhaw.www.repository;

import ch.zhaw.www.model.Prompt;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Public class to read Files of TXT type.
 * Class implements readFile method from the FileReader interface.
 */
@Repository
class TextFileReader implements FileReader {
    private static final Pattern PATTERN = Pattern.compile("\\bWALTER(E|T|TEN|TE|N|ST|chen)?\\b");
    private static final Logger LOGGER = Logger.getLogger(TextFileReader.class.getSimpleName());
    
    @Override
    public List<Prompt> readFile(File file) throws FileReaderError.WrongFileFormatException, IOException {
        try (Stream<String> stream = Files.lines(file.toPath())) {
            return stream
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
        } catch (FileReaderError e) {
            throw new FileReaderError.WrongFileFormatException();
        }
    }
    
    /**
     * Method to count the total number of WALTER words (or variations) that appear in one statement.
     *
     * @param input: sentence to be analysed.
     * @return total number of "WALTER" words.
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


