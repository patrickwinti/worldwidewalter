package ch.zhaw.www.dto;

import lombok.Data;

/**
 * Response object for POST /games. Contains the id of the newly created game and the host
 * player (the creator), who is registered as the first player.
 */
@Data
public class GameCreatedDto {
    private final String gameId;
    private final PlayerDto host;
}
