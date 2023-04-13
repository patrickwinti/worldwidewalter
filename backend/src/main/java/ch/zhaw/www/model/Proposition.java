package ch.zhaw.www.model;

import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Model class for the proposition sent by players
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Proposition {
    private static final String DELIMITER = "";
    
    @NotNull
    private final String id;
    @NotNull
    private final String playerId;
    private final List<String> gaps;
    private final List<Proposition> duplicates = new ArrayList<>();

    /**
     * Checks if the current proposition has the same gaps as the given proposition.
     *
     * @param proposition The proposition to compare gaps with.
     * @return true if both propositions have the same number of gaps and each pair of corresponding gaps has the same value, ignoring case and leading/trailing whitespace, false otherwise.
     */
    public boolean hasSameGaps(Proposition proposition) {
        return gaps.size() == proposition.gaps.size() &&
                IntStream.range(0, gaps.size())
                        .filter(i -> areGapsAtPositionTheSame(proposition, i))
                        .count() == gaps.size();
    }

    /**
     * Compares the gaps at the given position between the current proposition and the given proposition.
     *
     * @param proposition The proposition to compare gaps with.
     * @param i The position of the gap to compare.
     * @return true if the gaps at the specified position in both propositions are the same, ignoring case and leading/trailing whitespace, false otherwise.
     */
    private boolean areGapsAtPositionTheSame(final Proposition proposition, final int i) {
        return gaps.get(i).trim().equalsIgnoreCase(proposition.getGaps().get(i).trim());
    }
}

