package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class GameServiceImplTest {

    @Autowired
    private GameService gameService;
    @MockBean
    private GameRepository gameRepository;

    @Test
    void testAddGameSavesItToRepository() {
        var game = gameService.createGame();
        verify(gameRepository).save(game);
        assertNotNull(game.getId());
    }

    @Test
    void testGetGameReadsFromRepository_Found() {
        var expectedGame = mockGameInRepository();
        var actualGame = gameService.getGame(expectedGame.getId());

        assertEquals(expectedGame, actualGame);
    }

    @Test
    void testGetGameReadsFromRepository_NotFound() {
        var gameId = "Kjölasjflksdf";
        mockGameNotFoundInRepository(gameId);
        assertThrows(GameError.NotFoundException.class, () -> gameService.getGame(gameId));

    }

    @Test
    void enterGame() {
        // 1. mock a game in the repository
        var game = mockGameInRepository();
        // 2. call the method under test
        var player1 = gameService.enterGame(game.getId(), "Nora");
        var player2 = gameService.enterGame(game.getId(), "Nora");
        // 3. add player1 to the game in activePlayers
        game.getActivePlayers().put(player1.getId(), player1);
        // 4. remove player1 from the game in waitingRoom
        game.getWaitingRoom().remove(player1.getId());
        // 5. add another player with the same name
        var player3 = gameService.enterGame(game.getId(), "Nora");
        // 6. verify that 3 players were added to the game
        assertEquals(2, game.getWaitingRoom().size());
        assertEquals(1, game.getActivePlayers().size());
        assertEquals(player1, game.getActivePlayers().get(player1.getId()));
        assertEquals(player2, game.getWaitingRoom().get(player2.getId()));
        assertEquals(player3, game.getWaitingRoom().get(player3.getId()));
        // 7. verify that player 1 is in activePlayers and player 2 and 3 are in waitingRoom
        assertFalse(game.getWaitingRoom().containsKey(player1.getId()));
        assertTrue(game.getActivePlayers().containsKey(player1.getId()));
        assertTrue(game.getWaitingRoom().containsKey(player2.getId()));
        assertTrue(game.getWaitingRoom().containsKey(player3.getId()));
        // 8. verify that the name of player 2 was changed to "Nora 😎" and the name of player 3 was changed to "Nora 😋"
        assertEquals("Nora", game.getActivePlayers().get(player1.getId()).getName());
        assertEquals("Nora 😎", game.getWaitingRoom().get(player2.getId()).getName());
        assertEquals("Nora 😋", game.getWaitingRoom().get(player3.getId()).getName());
    }

    private Game mockGameInRepository() {
        var game = new Game(UUID.randomUUID().toString());
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        return game;
    }

    private void mockGameNotFoundInRepository(String gameId) {
        doThrow(GameError.NotFoundException.class)
                .when(gameRepository).findById(gameId);
    }
}