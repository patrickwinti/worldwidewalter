package ch.zhaw.www.service;

import ch.zhaw.www.model.Round;
import ch.zhaw.www.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static ch.zhaw.www.TestHelper.createGame;
import static ch.zhaw.www.TestHelper.createRound;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class EntityServiceTest {
    
    @MockitoBean
    private GameRepository gameRepository;
    @Autowired
    private EntityService entityService;
    
    @Test
    void findRoundIfExits() {
        var roundToBeFound = createRound();
        var game1 = createGame();
        game1.addRound(createRound());
        var game2 = createGame();
        var game3 = createGame();
        game3.addRound(createRound());
        game3.addRound(createRound());
        game3.addRound(roundToBeFound);
        game3.addRound(createRound());
        when(gameRepository.findAll()).thenReturn(List.of(game1, game2, game3));
        
        final Round gameForRound = entityService.getRound(roundToBeFound.getId());
        assertNotNull(gameForRound);
        assertSame(roundToBeFound, gameForRound);
    }
    
    @Test
    void findRoundIfItDoesNotExits() {
        var game1 = createGame();
        game1.addRound(createRound());
        var game2 = createGame();
        var game3 = createGame();
        game3.addRound(createRound());
        when(gameRepository.findAll()).thenReturn(List.of(game1, game2, game3));
        
        final String roundToBeFound = "round does not exist";
        assertThrows(RoundError.NotFoundException.class, () -> entityService.getRound(roundToBeFound));
    }
    
    @Test
    void findGameByRound() {
        var game1 = createGame();
        Round roundToBeFound = createRound();
        game1.addRound(createRound());
        game1.addRound(roundToBeFound);
        game1.addRound(createRound());
        var game2 = createGame();
        game2.addRound(createRound());
        when(gameRepository.findAll()).thenReturn(List.of(game1, game2));
        
        assertEquals(game1, entityService.getGameForRound(roundToBeFound.getId()));
    }
    
    @Test
    void findGameByRound_doesNotExist() {
        var game1 = createGame();
        game1.addRound(createRound());
        var game2 = createGame();
        game2.addRound(createRound());
        when(gameRepository.findAll()).thenReturn(List.of(game1, game2));
        
        final String roundToBeFound = "round does not exist";
        assertThrows(RoundError.NotFoundException.class, () -> entityService.getGameForRound(roundToBeFound));
    }
    
    @Test
    void saveGame_ExistsAlready() {
        var game = createGame();
        when(gameRepository.existsById(game.getId())).thenReturn(true);
        assertThrows(GameError.ExistAlready.class, () -> entityService.saveNewGame(game));
    }
}