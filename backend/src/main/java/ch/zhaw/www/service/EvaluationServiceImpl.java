package ch.zhaw.www.service;

import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EvaluationServiceImpl implements EvaluationService {
    private static final int NONE = 0;
    private static final int SINGLE_POINT = 1;


    // Technically all the parameters could be fetched from roundId but this would make testing a lot more costly.
    @Override
    public Map<String, Integer> evaluateSelection(Round round, String selectedPropositionId, String selectorId) {

        Map<String, Integer> points = new ConcurrentHashMap<>();
        String sphinxId = round.getSphinx().getId();
        round.getPropositions().forEach(proposition -> {
            if (selectedPropositionId.equals(proposition.getPropositionId()) && !proposition.getPlayerId().equals(sphinxId)) {
                points.put(proposition.getPlayerId(), SINGLE_POINT);
                round.setAtLeastOneNoneSphinxPropositionHasBeenSelected(true);
                points.put(sphinxId, round.getTempSphinxPoints());
                round.setTempSphinxPoints(0);
            }
            if (selectedPropositionId.equals(proposition.getPropositionId()) && proposition.getPlayerId().equals(sphinxId)
                    && round.getAtLeastOneNoneSphinxPropositionHasBeenSelected()) {
                points.put(proposition.getPlayerId(), SINGLE_POINT);
                points.put(sphinxId, SINGLE_POINT);

            } else if (selectedPropositionId.equals(proposition.getPropositionId()) && proposition.getPlayerId().equals(sphinxId)
                    && !round.getAtLeastOneNoneSphinxPropositionHasBeenSelected()) {
                points.put(proposition.getPlayerId(), SINGLE_POINT);
                round.setTempSphinxPoints(round.getTempSphinxPoints() + SINGLE_POINT);
            }
        });
        return points;
    }
}
