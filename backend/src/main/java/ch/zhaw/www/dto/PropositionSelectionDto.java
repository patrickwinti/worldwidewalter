package ch.zhaw.www.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response containing all proposition of the players
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropositionSelectionDto {
    private final Map<String, List<String>> propositions;
    @Nullable
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss", timezone = "UTC")
    private final Instant selectionSubmissionEndInUtc;
}
