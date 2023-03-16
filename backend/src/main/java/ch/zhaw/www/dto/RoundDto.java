package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data object representing the new round
 * with its prompt
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoundDto {
    @Valid
    @NotNull
    private final String id;
    @Valid
    @NotNull
    private final String prompt;
}
