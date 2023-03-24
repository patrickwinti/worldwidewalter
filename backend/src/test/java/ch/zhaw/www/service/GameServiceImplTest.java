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
        var player3 = gameService.enterGame(game.getId(), "Nora");

        // 3. verify that 3 players were added to the game
        assertEquals(3, game.getWaitingRoom().size());
        assertTrue(game.getWaitingRoom().containsKey(player1.getId()));
        assertEquals(player1, game.getWaitingRoom().get(player1.getId()));

        // 4. verify that the name of player 2 was changed to "Nora+1"
        assertEquals("Nora", game.getWaitingRoom().get(player1.getId()).getName());
        assertEquals("Nora+1", game.getWaitingRoom().get(player2.getId()).getName());
        assertEquals("Nora+1+1", game.getWaitingRoom().get(player3.getId()).getName());
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