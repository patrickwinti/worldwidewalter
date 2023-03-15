package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response object for API call
 * POST /games. Contains id and
 * relative path for a newly created
 * game.
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameDto {
    @Valid
    @NotNull
    String id;
    @Valid
    @NotNull
    String path;
}
