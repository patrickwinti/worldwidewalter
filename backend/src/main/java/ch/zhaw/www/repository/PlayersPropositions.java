package ch.zhaw.www.repository;

import ch.zhaw.www.model.Proposition;

import java.util.*;

public class PlayersPropositions {

    private List<Proposition> propositions = new ArrayList<>();

    /**
     * Creates a list of
     * @param gaps
     */
    public PlayersPropositions(List<String> gaps) {
        propositions = stringToProposition(gaps);
    }

    /*
     * This method converts the string proposition submission to a Proposition object.
     * It creates a unique ID for each string input, if two or more inputs are identical,
     * it adds the proposition to the duplicates list of the identical proposition submission
     */
    private List<Proposition> stringToProposition(List<String> gaps) {
        List<Proposition> props = new ArrayList<>();
        for (String gap : gaps) {
            String id = UUID.randomUUID().toString();
            Proposition temp = new Proposition(id, gap);

            if (props.isEmpty()) {
                props.add(temp);
            }

            for (int i = 0; i <= props.size(); i++) {
                if (gap.equalsIgnoreCase(props.get(i).getGap())) {
                    props.get(i).getDuplicates().add(temp);
                }
                props.add(temp);
            }
        }
        return props;
    }

}

