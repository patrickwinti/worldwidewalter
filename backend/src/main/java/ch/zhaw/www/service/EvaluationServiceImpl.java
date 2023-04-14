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
    private final EntityService entityService;

    public EvaluationServiceImpl(EntityService entityService) {
        this.entityService = entityService;
    }

    // Technically all the parameters could be fetched from roundId but this would make testing a lot more costly.
    @Override
    public Map<String, Integer> evaluateSelection(String roundId, String sphinxId, List<Proposition> propositions,
                                                  String selectedPropositionId, String propositionOriginatorIdOfSelection) {
        Round round = entityService.getRound(roundId);
        Map<String, Integer> points = new ConcurrentHashMap<>();
        propositions.forEach(proposition -> {
            if (selectedPropositionId.equals(proposition.getId()) && !propositionOriginatorIdOfSelection.equals(sphinxId)) {
                points.put(proposition.getPlayerId(), SINGLE_POINT);
                round.getAtLeastOneNoneSphinxPropositionHasBeenSelected().set(true);
                points.put(sphinxId, round.getTempSphinxPoints().get());
                round.getTempSphinxPoints().set(NONE);
            }
            if (selectedPropositionId.equals(proposition.getId()) && propositionOriginatorIdOfSelection.equals(sphinxId)
                    && round.getAtLeastOneNoneSphinxPropositionHasBeenSelected().get()) {
                points.put(proposition.getPlayerId(), SINGLE_POINT);
                points.put(sphinxId, SINGLE_POINT);

            } else if (selectedPropositionId.equals(proposition.getId()) && propositionOriginatorIdOfSelection.equals(sphinxId)
                    && !round.getAtLeastOneNoneSphinxPropositionHasBeenSelected().get()) {
                points.put(proposition.getPlayerId(), SINGLE_POINT);
                round.getTempSphinxPoints().set(SINGLE_POINT);
            }
        });
        return points;
    }
}
