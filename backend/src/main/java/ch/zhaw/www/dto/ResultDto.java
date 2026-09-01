package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Data object representing the results for round
 * The results contains the player name and their current points
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultDto {
    @NotNull
    List<RankingDto> ranking;
    @NotNull
    List<SelectionDto> selections;
}
