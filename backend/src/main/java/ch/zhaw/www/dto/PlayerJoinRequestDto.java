package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data object representing the player
 * wanting to join the game with given
 * player name.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerJoinRequestDto {
    @NotNull
    private String playerName;
}
