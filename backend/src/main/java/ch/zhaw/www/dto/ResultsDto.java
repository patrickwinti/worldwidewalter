package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * Data object representing the results for round
 * The results contains the player name and their current points
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultsDto {
    @NotNull
    private final Map<String, Integer> results;
}
