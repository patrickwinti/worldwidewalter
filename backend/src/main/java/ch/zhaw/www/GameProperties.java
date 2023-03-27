package ch.zhaw.www;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.time.Duration;

/**
 * Container for environment properties
 */
@Getter
@Configuration
@PropertySource("classpath:config.properties")
public class GameProperties {
    @Value("${round.proposition-submission-duration}")
    private Duration propositionSubmissionDuration;
    @Value("${round.enter-limit-duration}")
    private Duration roundEnterLimitDuration;
}