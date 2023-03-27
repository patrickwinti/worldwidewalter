package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

//todo implement as a list
/**
 * Model class for the proposition sent by players
 */
@Data
public class Proposition {
    @Id
    @NotNull
    private final String id;
    private final List<String> gaps;
    private List<Proposition> duplicates;
}
