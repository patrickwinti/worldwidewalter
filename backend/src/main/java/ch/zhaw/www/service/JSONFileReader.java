package ch.zhaw.www.service;
import ch.zhaw.www.model.Prompt;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
/**
 * Public class to read Files of JSON type.
 * Class implements FileReader interface.
 */
public class JSONFileReader implements FileReader {
    /**
     * Method to read file and return a list of Prompts from a JSON file
     *
     * @param file JSON file input
     * @return List with parsed prompts
     */
    public List<Prompt> readFile(File file) {
        try (BufferedReader fileReader = Files.newBufferedReader(file.toPath())) {
            ObjectMapper mapper = new ObjectMapper();
            Deck deck = mapper.readValue(fileReader, Deck.class);
            return deck.cards.values().stream()
                    .flatMap(Card::toStream)
                    .filter(Objects::nonNull)
                    .map(Prompt::new)
                    .toList();
        } catch (Exception e) {
            throw new FileReaderError.WrongFileFormatError();
        }
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class Deck {
        @JsonProperty("cards")
        private Map<String, Card> cards;
    }
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class Card {
        @JsonProperty("1")
        private String first;
        @JsonProperty("2")
        private String second;
        @JsonProperty("3")
        private String three;
        Stream<String> toStream() {
            return Stream.of(first, second, three);
        }
    }
}