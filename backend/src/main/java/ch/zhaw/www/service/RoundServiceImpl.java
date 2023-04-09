package ch.zhaw.www.service;

import ch.zhaw.www.GameProperties;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
class RoundServiceImpl implements RoundService {
    private static final int ONE_ROUND_LEFT = 1;
    
    private final GameProperties gameProperties;
    private final EntityService entityService;
    
    RoundServiceImpl(GameProperties gameProperties, EntityService entityService) {
        this.gameProperties = gameProperties;
        this.entityService = entityService;
    }
    
    @Override
    public void selectSphinx(Game game) {
        var round = game.getCurrentRound();
        if (round != null && round.getSphinx() == null) {
            var entries = game.getSphinxCandidates();
            if (entries.isEmpty()) return;
            var player = Collections.min(entries, Comparator.comparingInt(Map.Entry::getValue));
            
            entries.remove(player);
            
            Player sphinx = null;
            if (game.hasActivePlayer(player.getKey().getId())) {
                if (player.getValue() > ONE_ROUND_LEFT) {
                    entries.add(Map.entry(player.getKey(), player.getValue() - 1));
                }
                game.setSphinxCandidates(entries);
                sphinx = player.getKey();
            } else {
                selectSphinx(game);
            }
            round.setSphinx(sphinx);
        }
    }
    
    @Override
    public void createNewRound(final Game game) {
        game.addRound(new Round(UUID.randomUUID().toString(),
                game.consumePrompt(),
                gameProperties.getPropositionSubmissionDuration(),
                gameProperties.getRoundEnterLimitDuration(),
                gameProperties.getSelectionSubmissionDuration()));
        game.getAllPlayers()
                .filter(player -> game.hasActivePlayer(player.getId()))
                .takeWhile(player -> game.hasCapacityForNewActivePlayer())
                .forEach(game::moveToActivePlayers);
    }
    
    @Override
    public void submitProposition(String roundId, String playerId, List<String> gaps) {
        verifyPlayerIsActive(roundId, playerId);
        entityService.editRound(roundId, round -> {
            Proposition temp = new Proposition(UUID.randomUUID().toString(), playerId, gaps);
            boolean isDuplicate = false;
            for (Proposition proposition : round.getPropositions()) {
                if (proposition.hasSameGaps(temp)) {
                    proposition.getDuplicates().add(temp);
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate) {
                round.addProposition(temp);
            }
        });
    }
    
    @Override
    public void selectProposition(String roundId, String playerId, String propositionId) {
        verifyPlayerIsActive(roundId, playerId);
        
    }
    
    @Override
    public Round getRound(String roundId, String playerId) {
        verifyPlayerIsActive(roundId, playerId);
        return entityService.getRound(roundId);
    }
    
    private void verifyPlayerIsActive(String roundId, String playerId) {
        if (!entityService.isPlayerActiveInRound(roundId, playerId)) {
            throw new PlayerError.NotFoundException(playerId);
        }
    }
}
