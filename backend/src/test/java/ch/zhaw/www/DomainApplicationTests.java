package ch.zhaw.www;

import ch.zhaw.www.controller.GameController;
import ch.zhaw.www.service.EntityService;
import ch.zhaw.www.service.GameService;
import ch.zhaw.www.service.RoundService;
import ch.zhaw.www.utils.RandomProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DomainApplicationTests {

    @Autowired
    RandomProvider randomProvider;

    @Autowired
    GameController gameController;

    @Autowired
    EntityService entityService;

    @Autowired
    GameService gameService;

    @Autowired
    RoundService roundService;

    @Test
    void contextLoads() {
        assertThat(randomProvider).isNotNull();
        assertThat(gameController).isNotNull();
        assertThat(entityService).isNotNull();
        assertThat(gameService).isNotNull();
        assertThat(roundService).isNotNull();
    }

}
