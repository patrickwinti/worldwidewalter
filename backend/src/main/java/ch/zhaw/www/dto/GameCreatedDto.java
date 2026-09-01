package ch.zhaw.www.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Response object for POST /games. Contains the id of the newly created game and the host
 * player (the creator), who is registered as the first player.
 */
@Data
public class GameCreatedDto {
    @NotNull
    private final String gameId;
    @NotNull
    private final PlayerDto host;
}
