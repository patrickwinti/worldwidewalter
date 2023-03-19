package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    @Override
    public List<Prompt> readFile (File file) {

        ObjectMapper objectMapper = new ObjectMapper();
        List<Prompt> prompts = new ArrayList<>();

        try {
            JsonNode jsonNode = objectMapper.readTree(file);

            JsonNode cardsNode = jsonNode.get("cards");

            // Loop over the child nodes of the "cards" field
            for (JsonNode cardNode : cardsNode) {

                // Access the fields of each card using the get() method
                prompts.add(new Prompt(cardNode.get("1").asText()));
                prompts.add(new Prompt(cardNode.get("2").asText()));
                prompts.add(new Prompt(cardNode.get("3").asText()));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return prompts;
    }
}

