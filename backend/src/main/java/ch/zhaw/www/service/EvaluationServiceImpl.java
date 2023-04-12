package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EvaluationServiceImpl implements EvaluationService {
    public static final AtomicBoolean AT_LEAST_ONE_SPHINX_PROPOSITION_HAS_BEEN_SELECTED = new AtomicBoolean(false);
    public static final int NONE = 0;
    private static final int SINGLE_POINT = 1;
    private final EntityService entityService;
    private final GameService gameService;

    public EvaluationServiceImpl(EntityService entityService, GameService gameService) {
        this.entityService = entityService;
        this.gameService = gameService;
    }

    @Override
    public void evaluateRound(String gameId) {
        AtomicInteger tempSphinxPoints = new AtomicInteger();
        Game game = gameService.getGame(gameId);
        String sphinxId = game.getCurrentRound().getSphinx().getId();

        game.getCurrentRound().getSelections().forEach((key, value) -> {
            game.getCurrentRound().getPropositions().forEach(proposition -> {
                if (value.equals(proposition.getId()) && !key.equals(sphinxId)) {
                    addPoint(proposition.getPlayerId(), gameId);
                    AT_LEAST_ONE_SPHINX_PROPOSITION_HAS_BEEN_SELECTED.set(true);
                    addTempPointsToSphinx(gameId, tempSphinxPoints.get());
                    tempSphinxPoints.set(NONE);
                }
                if (value.equals(proposition.getId()) && key.equals(sphinxId) && AT_LEAST_ONE_SPHINX_PROPOSITION_HAS_BEEN_SELECTED.get()) {
                    addPoint(proposition.getPlayerId(), gameId);
                    addPoint(sphinxId, gameId);

                } else if (value.equals(proposition.getId()) && key.equals(sphinxId) && AT_LEAST_ONE_SPHINX_PROPOSITION_HAS_BEEN_SELECTED.get()) {
                    tempSphinxPoints.getAndIncrement();
                }
            });
        });
    }


    private void addPoint(String playerId, String gameId) {
        entityService.editGame(gameId, game -> {
            game.getPoints().put(playerId, SINGLE_POINT);
        });
    }

    private void addTempPointsToSphinx(String gameId, int points) {
        entityService.editGame(gameId, game -> {
            game.getPoints().put(game.getCurrentRound().getSphinx().getId(), points);
        });
    }
}
