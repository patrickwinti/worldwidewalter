package ch.zhaw.www.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Represents a player's ranking information.
 */
@Data
public class RankingDto {
    @NotNull
    private final String playerName;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final int points;
}
