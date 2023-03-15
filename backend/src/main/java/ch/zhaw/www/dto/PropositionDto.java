package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropositionDto {
    @Valid
    @NotNull
    String id;
    @Valid
    @NotNull
    String gap;
}
