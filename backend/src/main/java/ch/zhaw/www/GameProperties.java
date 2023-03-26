package ch.zhaw.www;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Container for environment properties
 */
@Getter
@Configuration
@PropertySource("classpath:config.properties")
public class GameProperties {
    @Value("${round.proposition-duration}")
    private int propositionSubmissionDuration;
    @Value("${round.enter-limit}")
    private int roundEnterLimit;
}