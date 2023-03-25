package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Map;


/**
 * Model class for the proposition sent by players
 */
@Data
public class Proposition {

    private final String placeholderReplacement;
    private Map<String, String> duplicates;
    @Id
    @NotNull
    private final String id;
}
