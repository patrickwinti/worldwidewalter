package ch.zhaw.www.service;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static ch.zhaw.www.TestHelper.createGame;
import static ch.zhaw.www.TestHelper.createRound;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class EntityServiceTest {
    
    @MockBean
    private GameRepository gameRepository;
    @Autowired
    private EntityService entityService;
    
    @Test
    void findGameByRoundIfExits() {
        var roundToBeFound = createRound();
        var game1 = createGame();
        game1.addRound(createRound());
        var game2 = createGame();
        var game3 = createGame();
        game3.addRound(roundToBeFound);
        when(gameRepository.findAll()).thenReturn(List.of(game1, game2, game3));
        
        final Game gameForRound = entityService.getRound(roundToBeFound.getId());
        assertNotNull(gameForRound);
        assertSame(game3, gameForRound);
    }
    
    @Test
    void findGameByRoundIfItDoesNotExits() {
        var game1 = createGame();
        game1.addRound(createRound());
        var game2 = createGame();
        var game3 = createGame();
        game3.addRound(createRound());
        when(gameRepository.findAll()).thenReturn(List.of(game1, game2, game3));
        
        final String roundToBeFound = "round does not exist";
        assertThrows(RoundError.NotFoundException.class, () -> entityService.getRound(roundToBeFound));
    }
}