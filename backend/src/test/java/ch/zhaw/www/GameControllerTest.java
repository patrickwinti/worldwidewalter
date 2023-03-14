package ch.zhaw.www;


import ch.zhaw.www.model.Game;
import ch.zhaw.www.service.GameService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebAppConfiguration
class GameControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private GameService gameService;

    @Test
    void testCreateGame_200() throws Exception {
        Mockito.when(gameService.createGame()).then(invocationOnMock -> new Game("1"));
        mvc.perform(MockMvcRequestBuilders.post("/api/games")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"1\",\"path\":\"/games/1\"}"));
    }
}