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
        Map<String, Integer> evaluation = new HashMap<>();
        String sphinxId = Objects.requireNonNull(round.getSphinx()).getId();
        round.getPropositions()
                .stream()
                .filter(proposition -> selectedPropositionId.equals(proposition.getId()))
                .findFirst()
                .ifPresent(proposition -> {
                    if (proposition.getPlayerId().equals(sphinxId)) {
                        evaluation.put(selectorId, SINGLE_POINT);
                        if (round.isAtLeastOneNoneSphinxPropositionHasBeenSelected()) {
                            evaluation.put(sphinxId, SINGLE_POINT);
                        } else {
                            round.setTempSphinxPoints(round.getTempSphinxPoints() + SINGLE_POINT);
                        }
                    } else {
                        evaluation.put(proposition.getPlayerId(), SINGLE_POINT);
                        evaluation.put(sphinxId, round.getTempSphinxPoints());
                        round.setTempSphinxPoints(ZERO);
                        round.setAtLeastOneNoneSphinxPropositionHasBeenSelected(true);
                    }
                });
        return evaluation;
    }
}
