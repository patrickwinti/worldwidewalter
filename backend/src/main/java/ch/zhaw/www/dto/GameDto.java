package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Response object for API call
 * POST /games. Contains id and
 * relative path for a newly created
 * game.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameDto {
    @Valid
    @NotNull
    private final String id;
}
