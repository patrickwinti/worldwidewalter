package ch.zhaw.www.service;

import ch.zhaw.www.repository.GameRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import static ch.zhaw.www.utils.InstantWrapper.isInFuture;

@Service
public class CleanUpService {
    private final Logger logger = Logger.getLogger(CleanUpService.class.getSimpleName());
    
    @Value("${cleanup.rate}")
    private long cleanUpTimeInMinutes;
    private final Duration cleanUpTimeDurationInMinutes;
    private final GameRepository repository;
    
    public CleanUpService(GameRepository repository) {
        this.repository = repository;
        this.cleanUpTimeDurationInMinutes = Duration.of(cleanUpTimeInMinutes, ChronoUnit.MINUTES);
    }
    
    @Scheduled(fixedRateString = "${cleanup.rate}", timeUnit = TimeUnit.MINUTES)
    private void runCleanUp() {
        logger.info("running game cleanup");
        synchronized (repository) {
            StreamSupport.stream(this.repository.findAll().spliterator(), false)
                    .filter(game -> !isInFuture(game.getLastEdit().plus(cleanUpTimeDurationInMinutes)))
                    .forEach(game -> {
                        this.repository.delete(game);
                        logger.info("deleted game: " + game.getId());
                    });
        }
    }
}
