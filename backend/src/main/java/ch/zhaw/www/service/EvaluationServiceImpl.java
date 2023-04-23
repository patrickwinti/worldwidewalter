package ch.zhaw.www.service;

import ch.zhaw.www.model.Round;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class EvaluationServiceImpl implements EvaluationService {
    private static final int ZERO = 0;
    private static final int SINGLE_POINT = 1;

    @Override
    public Map<String, Integer> evaluateSelection(Round round, String selectedPropositionId, String selectorId) {
        checkIfLegalSelection(round, selectedPropositionId, selectorId);
        Map<String, Integer> evaluation = new HashMap<>();
        String sphinxId = Objects.requireNonNull(round.getSphinx()).getId();

        round.getPropositions()
                .stream()
                .filter(proposition -> selectedPropositionId.equals(proposition.getId()))
                .findFirst()
                .ifPresent(proposition -> {
                    if (proposition.getPlayerIds().size() == 1) {
                        if (proposition.getPlayerIds().get(0).equals(sphinxId)) {
                    if (proposition.getPlayerIds().size() > 1) {
                        evaluation.put(selectorId, SINGLE_POINT);
                    }
                            if (round.isHasNonSphinxPropositionBeenSelected()) {
                                evaluation.put(sphinxId, SINGLE_POINT);
                            } else {
                                round.setTempSphinxPoints(round.getTempSphinxPoints() + SINGLE_POINT);
                            }
                        } else {
                            if (!selectorIdHasSubmittedDoubleProposition(round, selectorId)) {
                                evaluation.put(proposition.getPlayerIds().get(0), SINGLE_POINT);
                            }
                            evaluation.put(sphinxId, round.getTempSphinxPoints());
                            round.setTempSphinxPoints(ZERO);
                            round.setHasNonSphinxPropositionBeenSelected(true);
                        }
                    } else if (proposition.getPlayerIds().size() > 1 && proposition.getPlayerIds().contains(sphinxId)) {
                        evaluation.put(selectorId, SINGLE_POINT);
                    }

                });
        return evaluation;
    }

    private void checkIfLegalSelection(Round round, String selectedPropositionId, String selectorId) {

        round.getPropositions()
                .stream()
                .filter(proposition -> selectedPropositionId.equals(proposition.getId()))
                .findFirst()
                .ifPresent(proposition -> {
                    if (proposition.getPlayerIds().size() == 1) {
                        if (proposition.getPlayerIds().get(0).equals(selectorId)) {
                            throw new SelectionError.IllegalSelection(selectedPropositionId);
                        }
                    }
                });
    }

    private void isSubmittedBy(Proposition proposition, String proposerId) {

    }

    private boolean selectorIdHasSubmittedDoubleProposition(Round round, String selectorId) {
        return round.getPropositions()
                .stream()
                .filter(proposition -> proposition.getPlayerIds().size() > 1)
                .anyMatch(proposition -> proposition.getPlayerIds().contains(selectorId));
    }

}