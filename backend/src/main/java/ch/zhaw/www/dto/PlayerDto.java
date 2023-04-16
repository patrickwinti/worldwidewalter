package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data object representing the player id
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerDto {
    @Valid
    @NotNull
    private final String id;

    @Nullable
    private String playerName;
}

