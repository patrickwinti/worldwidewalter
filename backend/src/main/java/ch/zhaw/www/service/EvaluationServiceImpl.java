package ch.zhaw.www.service;

import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class EvaluationServiceImpl implements EvaluationService {
    private static final int ZERO = 0;
    private static final int SINGLE_POINT = 1;

    @Override
    public Map<String, Integer> evaluateSelection(Round round, String selectedPropositionId, String selectorId) {
        checkIfLegalSelection(round, selectedPropositionId, selectorId);
        Map<String, Integer> evaluation = new HashMap<>();
        String sphinxId = Objects.requireNonNull(round.getSphinx()).getId();

        findProposition(round, selectedPropositionId)
                .ifPresent(proposition -> {
                    if (!proposition.hasDuplicates()) {
                        String proposerId = proposition.getPlayerIds().get(0);
                        if (proposition.hasProposer(sphinxId)) {
                            evaluation.put(selectorId, SINGLE_POINT);
                            if (round.isHasNonSphinxPropositionBeenSelected()) {
                                evaluation.put(sphinxId, SINGLE_POINT);
                            } else {
                                round.setTempSphinxPoints(round.getTempSphinxPoints() + SINGLE_POINT);
                            }
                        } else {
                            evaluation.put(proposerId, SINGLE_POINT);
                            evaluation.put(sphinxId, round.getTempSphinxPoints());
                            round.setTempSphinxPoints(ZERO);
                            round.setHasNonSphinxPropositionBeenSelected(true);
                        }
                    } else if (proposition.hasProposer(sphinxId)) {
                        evaluation.put(selectorId, SINGLE_POINT);

                    } else {
                        evaluation.put(sphinxId, round.getTempSphinxPoints());
                        round.setTempSphinxPoints(ZERO);
                        round.setHasNonSphinxPropositionBeenSelected(true);
                    }

                });
        return evaluation;
    }

    private static Optional<Proposition> findProposition(Round round, String selectedPropositionId) {
        return round.getPropositions()
                .stream()
                .filter(proposition -> selectedPropositionId.equals(proposition.getId()))
                .findFirst();
    }

    private void checkIfLegalSelection(Round round, String selectedPropositionId, String selectorId) {

        findProposition(round, selectedPropositionId)
                .ifPresent(proposition -> {
                    if (!proposition.hasDuplicates() && proposition.hasProposer(selectorId)) {
                        throw new SelectionError.IllegalSelection(selectedPropositionId);
                    }
                    if (selectorId.equals(round.getSphinx().getId())){
                        throw new SelectionError.IllegalSelector(selectorId);
                    }
                });
    }
}