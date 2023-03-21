package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.model.Round;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
class GameServiceImpl implements GameService {
    @Override
    public Game createGame() {
        return new Game(UUID.randomUUID().toString());
    }

    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        return new Game(gameId);
    }

    @Override
    public Player enterGame(String gameId, String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {
        return new Player(playerName);
    }

    @Override
    public void leaveGame(String gameId, String playerId) throws GameError.NotFoundException {

    }

    @Override
    public Round startNextRound(String gameId) throws GameError.NotFoundException, RoundError.OngoingException,
            GameError.NotEnoughPlayersException {
        return new Round(UUID.randomUUID().toString(), new Prompt("I've always wanted to WALTER"));
    }

    @Override
    public void submitProposition(String roundId, String playerId, List<String> gaps) throws GameError.NotFoundException,
            RoundError.NotFoundException, PlayerError.NotFoundException {

    }

    @Override
    public void selectProposition(String roundId, String playerId, String propositionId) throws GameError.NotFoundException,
            RoundError.NotFoundException, PlayerError.NotFoundException, PropositionError.NotFoundException {

    }
}
