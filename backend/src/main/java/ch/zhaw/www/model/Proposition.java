package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;


/**
 * Model class for the proposition sent by players
 */
@Data
public class Proposition {

    private final List<String> gaps;
    private Map<String, String> duplicates;
    @Id
    @NotNull
    private final String id;

    public static boolean checkForDuplicate (List<Proposition> propositionsA, List<Proposition> propositionsB) {
            return propositionsA.size() == propositionsB.size() &&
                    propositionsA.stream().allMatch(propositionsB::contains);
    }
}
