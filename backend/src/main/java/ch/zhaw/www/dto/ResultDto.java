package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data object representing the results for round
 * The results contains the player name and their current points
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultDto {
    @NotNull
    private final String playerName;
    private final int points;
}
