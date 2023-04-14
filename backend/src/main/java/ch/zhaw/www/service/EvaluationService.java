package ch.zhaw.www.service;

import ch.zhaw.www.model.Proposition;

import java.util.List;
import java.util.Map;

/**
 * Service for evaluating the submitted selections for a round
 */
public interface EvaluationService {
    Map<String, Integer> evaluateSelection(String roundId, String sphinxId, List<Proposition> propositions,
                                           String selectedPropositionId, String propositionOriginatorIdOfSelection) throws GameError.NotFoundException, RoundError.NotFoundException;
}
