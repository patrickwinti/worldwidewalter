package ch.zhaw.www;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.repository.GameRepository;
import ch.zhaw.www.service.GameError;
import ch.zhaw.www.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class GameServiceTest {
    
    @Autowired
    private GameService gameService;
    @MockBean
    private GameRepository gameRepository;
    
    @Test
    void testAddGameSavesItToRepository() {
        var game = gameService.createGame();
        verify(gameRepository).saveNewGame(game);
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
        var gameId = "jibberish";
        mockGameNotFoundInRepository(gameId);
        assertThrows(GameError.NotFoundException.class, () -> gameService.getGame(gameId));
    }
    
    private Game mockGameInRepository() {
        var game = new Game(UUID.randomUUID().toString());
        
        doNothing().when(gameRepository).editGame(eq(game.getId()), any());
        when(gameRepository.getGame(game.getId())).thenReturn(game);
        
        return game;
    }
    
    private void mockGameNotFoundInRepository(String gameId) {
        doThrow(GameError.NotFoundException.class)
                .when(gameRepository).editGame(eq(gameId), any());
        
        doThrow(GameError.NotFoundException.class)
                .when(gameRepository).getGame(gameId);
    }
}