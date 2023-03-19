package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.File;
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
    @Operation(summary = "Creates a new game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/games", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public List<Prompt> readFile (File file) {

        ObjectMapper objectMapper = new ObjectMapper();
        List<Prompt> prompts = new ArrayList<>();
// todo add expected format checker
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
            throw new RuntimeException(e);
        }
        return prompts;
    }
}

