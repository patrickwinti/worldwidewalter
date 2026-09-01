package ch.zhaw.www.dto;

import ch.zhaw.www.model.Game;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Snapshot of the game lobby: the players who have joined, who the host is, whether the game
 * has started and how many players are needed to start. Sent over the {@code /topic/games/{id}/lobby}
 * WebSocket topic whenever the lobby changes and returned by GET /games/{id}/lobby.
 */
@Data
public class LobbyDto {
    @NotNull
    private final List<PlayerDto> players;
    @NotNull
    private final String hostId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final boolean started;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final int minimumPlayers;

    /**
     * Builds a lobby snapshot from the current game state.
     *
     * @param game the game to map
     * @return the lobby snapshot
     */
    public static LobbyDto from(Game game) {
        List<PlayerDto> players = game.getAllPlayers()
                .map(player -> new PlayerDto(player.getId(), player.getName()))
                .toList();
        return new LobbyDto(players, game.getHostId(), game.isStarted(), game.getMinimumAmountOfPlayers());
    }
}
