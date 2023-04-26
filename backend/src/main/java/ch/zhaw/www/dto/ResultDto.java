package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    List<RankingDto> ranking;
    List<SelectionDto> selections;
}

