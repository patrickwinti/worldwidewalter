package ch.zhaw.www.service;

import ch.zhaw.www.repository.GameRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import static ch.zhaw.www.utils.InstantWrapper.isInFuture;

@Service
public class CleanUpService {
    private final Logger logger = Logger.getLogger(CleanUpService.class.getSimpleName());
    
    @Value("${game.idle-time-before-removal}")
    private Duration gameIdleTimeBeforeRemoval;
    private final GameRepository repository;
    
    public CleanUpService(GameRepository repository) {
        this.repository = repository;
    }
    
    @Scheduled(fixedRateString = "${clean-up-service.game-cleanup.interval}", timeUnit = TimeUnit.MINUTES)
    protected void runGameCleanUp() {
        logger.info("running game cleanup");
        synchronized (repository) {
            StreamSupport.stream(this.repository.findAll().spliterator(), false)
                    .filter(game -> !isInFuture(game.getLastEdit().plus(gameIdleTimeBeforeRemoval)))
                    .forEach(game -> {
                        this.repository.delete(game);
                        logger.info("deleted game: " + game.getId());
                    });
        }
    }
}
