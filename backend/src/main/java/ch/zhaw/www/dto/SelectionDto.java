package ch.zhaw.www.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * The object contains a proposition, its authors and selectors
 */
@Data
@AllArgsConstructor
public class SelectionDto {
    @NotNull
    List<String> authors;
    @NotNull
    List<String> gaps;
    @NotNull
    List<String> selectors;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    boolean sphinxResponse;
}
