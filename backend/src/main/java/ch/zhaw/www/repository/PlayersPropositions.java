package ch.zhaw.www.repository;

import ch.zhaw.www.model.Proposition;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class PlayersPropositions {

    private List<Proposition> propositions = new ArrayList<>();

    /*
     * This method converts the string proposition submission to a Proposition object.
     * It creates a unique ID for each string input, if two or more inputs are identical,
     * it adds the proposition to the duplicates list of the identical proposition submission
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

    private boolean checkForDuplicates(List<String> list1, List<String> list2) {
        return list1.size() == list2.size() &&
                list1.stream()
                        .map(String::toLowerCase)
                        .toList()
                        .equals(list2.stream()
                                .map(String::toLowerCase)
                                .collect(Collectors.toList()));
    }

}

