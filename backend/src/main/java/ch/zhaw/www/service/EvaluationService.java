package ch.zhaw.www.service;

/**
 * Service for evaluating the submitted selections for a round
 */
public interface EvaluationService {
    void evaluateRound(String GameId) throws GameError.NotFoundException, RoundError.NotFoundException;
}
