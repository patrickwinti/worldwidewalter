package ch.zhaw.www;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.time.Duration;

/**
 * Container for environment properties
 */
@Getter
@Configuration
public class GameProperties {
    @Value("${round.proposition-submission-duration}")
    private Duration propositionSubmissionDuration;
    @Value("${round.enter-limit-duration}")
    private Duration roundEnterLimitDuration;
    @Value("${round.selection-submission-duration}")
    private Duration selectionSubmissionDuration;
    @Value("${round.continue-wait-duration}")
    private Duration continueWaitDuration;
    @Value("${game.maximum-players}")
    private int maximumAmountOfActivePlayersPerGame;
    @Value("${game.minimum-players}")
    private int minimumAmountOfActivePlayersPerGame;
    @Value("${prompt.deck-default.location}")
    private Resource defaultDeck;
}