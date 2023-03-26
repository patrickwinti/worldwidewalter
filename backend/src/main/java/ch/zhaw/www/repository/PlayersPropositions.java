package ch.zhaw.www.repository;

import ch.zhaw.www.model.Proposition;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to convert the entered the propositions by players into a proposition object and store them in a List of propositions
 */
@Data
public class PlayersPropositions {

    private List<Proposition> propositions = new ArrayList<>();

    /**
     * This method updates the current player propositions within a round. If a list is given as a parameter
     * for a proposition that already exists in the propositions list, the given parameter is not added to the propositions
     * list but rather to the duplicates list of the already existing proposition on the list.
     * Method automatically assigns a uniqueID to each new proposition.
     * @param gaps list of String with the suggestions for all players in the round
     */
    public void updatePropositions(List<String> gaps) {
        String id = UUID.randomUUID().toString();
        Proposition temp = new Proposition(id, gaps);

        if (propositions.isEmpty()) {
            propositions.add(temp);
            return;
        }
        for (Proposition proposition : propositions) {
            if (checkForDuplicates(proposition.getGap(), gaps)) {
                proposition.getDuplicates().add(temp);
                return;
            }
        }
        propositions.add(temp);
    }

    /*
     * Checks if the given list in the update propositions method already exists in the propositions list.
     */
    private boolean checkForDuplicates(List<String> existingProposition, List<String> newProposition) {
        return existingProposition.size() == newProposition.size() &&
                existingProposition.stream()
                        .map(String::toLowerCase)
                        .toList()
                        .equals(newProposition.stream()
                                .map(String::toLowerCase)
                                .collect(Collectors.toList()));
    }

}

