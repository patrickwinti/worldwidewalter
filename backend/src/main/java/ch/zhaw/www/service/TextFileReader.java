package ch.zhaw.www.service;

import ch.zhaw.www.model.Prompt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Public class to read Files of TXT type.
 * Class implements readFile method from the FileReader interface.
 */
public class TextFileReader implements FileReader {

    /**
     * Method to read file and return a list of Prompts from a JSON file
     *
     * @param file TXT file input
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
    public List<Prompt> readFile(File file) {
        List<Prompt> prompts = new ArrayList<>();
        try {
            Files.lines(file.toPath())
                    .filter(line -> !line.trim().isEmpty())
                    .forEach(line -> {
                        try {
                            prompts.add(new Prompt(line));
                        } catch (Exception e) {
                            System.err.println("Error processing line: " + line + ". No WALTER placeholders found");
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return prompts;
    }
}

