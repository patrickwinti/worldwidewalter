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
    @NotNull
    private final String id;
    private final List<String> gaps;
    private final List<String> playerIds = new ArrayList<>();

    /**
     * Adds player that submitted the given proposition.
     * Players that submitted duplicates of the prompt
     * will be added to the player IDs.
     * <p>
     * Submitting the same proposition twice (e.g. a retry, or a submission that races the
     * expiring round timer) does not count the player twice.
     *
     * @param playerId player identifier
     */
    public void submittedBy(final String playerId) {
        if (!playerIds.contains(playerId)) {
            playerIds.add(playerId);
        }
    }

    /**
     * Checks if the current proposition has the same gaps as the given proposition.
     *
     * @param otherGaps The gaps of other proposition to compare with.
     * @return true if both propositions have the same number of gaps and each pair of corresponding gaps has the same
     * value, ignoring case and leading/trailing whitespace, false otherwise.
     */
    public boolean hasSameGaps(List<String> otherGaps) {
        return otherGaps.size() == gaps.size() &&
                otherGaps.size() == IntStream.range(0, gaps.size())
                        .filter(i -> otherGaps.get(i).trim().equalsIgnoreCase(gaps.get(i).trim()))
                        .count();
    }

    /**
     * Checks if a given player submitted the current proposition.
     * @param playerId player identifier
     * @return true if the player submitted the proposition, false otherwise.
     */
    public boolean hasSubmitter(final String playerId) {
        return playerIds.contains(playerId);
    }

    /**
     * Checks if the current proposition has duplicates.
     * @return true if the proposition has duplicates, false otherwise.
     */
    public boolean hasDuplicates() {
        return playerIds.size() > 1;
    }

}