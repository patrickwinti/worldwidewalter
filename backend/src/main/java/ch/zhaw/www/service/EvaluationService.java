package ch.zhaw.www.service;

import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;

import java.util.List;
import java.util.Map;

/**
 * Service for evaluating the submitted selections for a round
 */
public interface EvaluationService {
    Map<String, Integer> evaluateSelection(Round round, String selectedPropositionId, String selectorId) throws RoundError.NotFoundException;
}
