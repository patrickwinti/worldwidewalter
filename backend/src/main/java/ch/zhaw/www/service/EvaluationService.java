package ch.zhaw.www.service;

import ch.zhaw.www.model.Round;

import java.util.Map;

/**
 * Service for evaluating a submitted selection in a round. It is responsible for point distribution.
 */
public interface EvaluationService {

    /**
     * This method evaluates the selected proposition in a round of the game and returns a map
     * containing the player IDs and the corresponding points earned in the evaluation.
     *
     * @param round                 The round to evaluate.
     * @param selectedPropositionId The ID of the selected proposition to evaluate.
     * @param selectorId            The ID of the player who selected the proposition.
     * @return A map containing the player IDs and the corresponding points earned in the evaluation.
     */
    Map<String, Integer> evaluateSelection(Round round, String selectedPropositionId, String selectorId) throws SelectionError.IllegalSelection, SelectionError.IllegalSelector;
}
