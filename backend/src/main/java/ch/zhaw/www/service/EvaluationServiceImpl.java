package ch.zhaw.www.service;

import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EvaluationServiceImpl implements EvaluationService {
    private static final int ZERO = 0;
    private static final int SINGLE_POINT = 1;

    @Override
    public Map<String, Integer> evaluateSelection(Round round, String selectedPropositionId, String selectorId) {
        Map<String, Integer> evaluation = new HashMap<>();
        String sphinxId = Objects.requireNonNull(round.getSphinx()).getId();

        Optional<Proposition> optionalProposition = findProposition(round, selectedPropositionId);
        if (optionalProposition.isPresent()) {
            Proposition proposition = optionalProposition.get();

            if (!handleAndPunishIllegalSelection(proposition, sphinxId, selectorId, evaluation).isEmpty()) {
                return evaluation;
            }

            List<String> submitterIds = proposition.getPlayerIds();
            if (submitterIds.contains(sphinxId)) {
                evaluation.put(selectorId, SINGLE_POINT);
            } else {
                round.setHasNonSphinxPropositionBeenSelected(true);
            }

            if (submitterIds.size() == 1) {
                String submitterId = submitterIds.get(0);
                if (submitterId.equals(sphinxId)) {
                    round.setTempSphinxPoints(round.getTempSphinxPoints() + SINGLE_POINT);
                } else {
                    evaluation.put(submitterId, SINGLE_POINT);
                }
            }

            if (round.isHasNonSphinxPropositionBeenSelected()) {
                evaluation.put(sphinxId, round.getTempSphinxPoints());
                round.setTempSphinxPoints(ZERO);
            }
        }
        return evaluation;
    }


    private static Optional<Proposition> findProposition(Round round, String selectedPropositionId) {
        return round.getPropositions()
                .stream()
                .filter(proposition -> selectedPropositionId.equals(proposition.getId()))
                .findFirst();
    }

    private Map<String, Integer> handleAndPunishIllegalSelection(Proposition proposition, String sphinxId, String selectorId, Map<String, Integer> evaluation) {
        if (!proposition.hasDuplicates() && proposition.hasSubmitter(selectorId)) {
            evaluation.put(selectorId, ZERO);
        }
        if (selectorId.equals(sphinxId)) {
            evaluation.put(selectorId, ZERO);
        }
        return evaluation;
    }
}