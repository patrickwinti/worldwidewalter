package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.model.Turn;
import ch.zhaw.www.repository.GameRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;

    GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public Game createGame() {
        var game = new Game(UUID.randomUUID().toString());
        saveGame(game);
        return game;
    }

    @Override
    public Game getGame(String gameId) throws GameError.NotFoundException {
        return findGame(gameId);
    }


    @Override
    public Player enterGame(String gameId, String playerName) throws GameError.NotFoundException, GameError.FullCapacityException {

        if (findGame(gameId).getWaitingRoom().containsKey(playerName)) {
            playerName = playerName + "+1";
            enterGame(gameId, playerName);
        }
        findGame(gameId).getWaitingRoom().put(playerName, new Player(playerName, UUID.randomUUID().toString()));
        return findGame(gameId).getWaitingRoom().get(playerName);
    }

    @Override
    public void leaveGame(String gameId, String playerId) throws GameError.NotFoundException {
        if (findGame(gameId) != null) {
            if (findGame(gameId).getWaitingRoom().containsKey(playerId)) {
                findGame(gameId).getWaitingRoom().remove(playerId);
            }
            if (findGame(gameId).getActivePlayers().containsKey(playerId)) {
                findGame(gameId).getActivePlayers().remove(playerId);
            }
        }
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

    private Game findGame(String gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new GameError.NotFoundException(gameId));
    }

    private void saveGame(Game game) {
        gameRepository.save(game);
    }
    public void selectSphinx(String gameId,Turn turn) throws GameError.NotFoundException

    {   int index = new Random().nextInt(findGame(gameId).getWaitingRoom().size());
        if(findGame(gameId).getPreviousSphinx().containsKey(findGame(gameId).getWaitingRoom().get(index)));
        {selectSphinx(gameId,turn);}
        Player newSphinx = findGame(gameId).getWaitingRoom().get(index);
        turn.setSphinx(newSphinx);
    }

}
