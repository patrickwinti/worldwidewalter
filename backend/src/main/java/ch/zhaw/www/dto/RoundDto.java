package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

/**
 * Data object representing the round
 * with its prompt and proposition submission end date
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoundDto {
    @NotNull
    private final String id;
    @NotNull
    private final String prompt;
    @Nullable
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss", timezone = "UTC")
    private final Instant endOfSubmissionsInUtc;
}
