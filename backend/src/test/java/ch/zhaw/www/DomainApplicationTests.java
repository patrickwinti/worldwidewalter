package ch.zhaw.www;

import ch.zhaw.www.bean.PostfixGenerator;
import ch.zhaw.www.controller.GameController;
import ch.zhaw.www.service.EntityService;
import ch.zhaw.www.service.GameService;
import ch.zhaw.www.service.RoundService;
import ch.zhaw.www.utils.EightAlphanumericGameIdGeneratorImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DomainApplicationTests {

    @Autowired
    PostfixGenerator postfixGenerator;

    @Autowired
    GameController gameController;

    @Autowired
    EntityService entityService;

    @Autowired
    GameService gameService;

    @Autowired
    RoundService roundService;

    @Autowired
    EightAlphanumericGameIdGeneratorImpl eightAlphanumericGameIdGeneratorImpl;

    @Test
    void contextLoads() {
        assertThat(postfixGenerator).isNotNull();
        assertThat(gameController).isNotNull();
        assertThat(entityService).isNotNull();
        assertThat(gameService).isNotNull();
        assertThat(roundService).isNotNull();
        assertThat(eightAlphanumericGameIdGeneratorImpl).isNotNull();
    }

}
