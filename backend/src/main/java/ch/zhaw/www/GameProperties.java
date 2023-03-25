package ch.zhaw.www;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource("classpath:config.properties")
public class GameProperties {
    @Value("${round.proposition-interval}")
    private int propositionSubmissionInterval;
}