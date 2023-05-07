package ch.zhaw.www.service;

import ch.zhaw.www.TestHelper;
import ch.zhaw.www.TimeHelper;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Duration;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
class CleanUpServiceTest {
    @Autowired
    private CleanUpService cleanUpService;
    
    @MockBean
    private GameRepository gameRepository;
    
    @Value("${game.idle-time-before-removal}")
    private Duration gameIdleTimeBeforeRemoval;
    
    private final Duration ONE_MINUTE = Duration.ofMinutes(1);
    
    @Test
    void cleanUpInactiveGameAndSkipActiveGame() {
        TimeHelper.enableFixedClocked();
        
        Game inactiveGame = TestHelper.createGame();
        Game activeGame = TestHelper.createGame();
        activeGame.setLastEdit(TimeHelper.getFixedClockInstant().plus(ONE_MINUTE));
        inactiveGame.setLastEdit(TimeHelper.getFixedClockInstant().minus(ONE_MINUTE));
        
        when(gameRepository.findAll()).thenReturn(List.of(activeGame, inactiveGame));
        
        TimeHelper.offsetFixedClockBy(gameIdleTimeBeforeRemoval);
        
        cleanUpService.runGameCleanUp();
        
        verify(gameRepository).delete(inactiveGame);
        verify(gameRepository, never()).delete(activeGame);
    }
}