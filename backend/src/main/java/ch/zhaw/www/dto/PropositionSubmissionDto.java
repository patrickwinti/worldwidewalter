package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Data object representing the submission of the
 * player proposition.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropositionSubmissionDto {
    @Valid
    @NotNull
    private List<String> gaps;
}
