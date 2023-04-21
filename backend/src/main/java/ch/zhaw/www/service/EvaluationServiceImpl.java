package ch.zhaw.www.service;

import ch.zhaw.www.model.Round;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EvaluationServiceImpl implements EvaluationService {
    private static final int NONE = 0;
    private static final int SINGLE_POINT = 1;


    @Override
    public Map<String, Integer> evaluateSelection(Round round, String selectedPropositionId, String selectorId) {

        Map<String, Integer> evaluation = new HashMap<>();
        String sphinxId = round.getSphinx().getId();
        round.getPropositions().forEach(proposition -> {
            if (selectedPropositionId.equals(proposition.getPropositionId()) && !proposition.getPlayerId().equals(sphinxId)) {
                evaluation.put(proposition.getPlayerId(), SINGLE_POINT);
                round.setAtLeastOneNoneSphinxPropositionHasBeenSelected(true);
                evaluation.put(sphinxId, round.getTempSphinxPoints());
                round.setTempSphinxPoints(NONE);
            }
            if (selectedPropositionId.equals(proposition.getPropositionId()) && proposition.getPlayerId().equals(sphinxId)
                    && round.getAtLeastOneNoneSphinxPropositionHasBeenSelected()) {
                evaluation.put(selectorId, SINGLE_POINT);
                evaluation.put(sphinxId, SINGLE_POINT);

            } else if (selectedPropositionId.equals(proposition.getPropositionId()) && proposition.getPlayerId().equals(sphinxId)
                    && !round.getAtLeastOneNoneSphinxPropositionHasBeenSelected()) {
                evaluation.put(selectorId, SINGLE_POINT);
                round.setTempSphinxPoints(round.getTempSphinxPoints() + SINGLE_POINT);
            }
        });
        return evaluation;
    }
}
