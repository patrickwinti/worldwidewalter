package ch.zhaw.www;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Prompt;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.service.GameError;
import ch.zhaw.www.service.GameService;
import ch.zhaw.www.service.PlayerError;
import ch.zhaw.www.service.RoundError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebAppConfiguration
class GameControllerTest {
    private static final String GAME_ID = "123";
    private static final String PLAYER_ID = "456";
    private static final String ROUND_ID = "789";
    private static final String PROPOSITION_ID = "-0-";
    private static final String HEADER_PLAYER = "X-PLAYER-ID";
    private static final Prompt PROMPT = new Prompt("prompt");
    
    @Autowired
    private MockMvc mvc;
    @MockBean
    private GameService gameService;
    
    @Test
    void testCreateGame_200() throws Exception {
        when(gameService.createGame()).then(invocationOnMock -> new Game(GAME_ID));
        mvc.perform(MockMvcRequestBuilders.post("/api/games"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + GAME_ID + "\",\"state\":\"WAITING_FOR_PLAYERS\"}"));
    }
    
    @Test
    void testGetGame_200() throws Exception {
        when(gameService.getGame(any())).then(invocationOnMock -> new Game(GAME_ID));
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}", GAME_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + GAME_ID + "\",\"state\":\"WAITING_FOR_PLAYERS\"}"));
    }
    
    @Test
    void testEnterGame_200() throws Exception {
        when(gameService.enterGame(any(), any())).then(m -> new Player(PLAYER_ID));
        
        mvc.perform(MockMvcRequestBuilders.post("/api/games/{gameId}/players", GAME_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\":\"Ulisses\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + PLAYER_ID + "\"}"));
        verify(gameService).enterGame(GAME_ID, "Ulisses");
    }
    
    @Test
    void testEnterGame_404() throws Exception {
        when(gameService.enterGame(any(), any())).thenThrow(new GameError.NotFoundException(GAME_ID));
        
        mvc.perform(MockMvcRequestBuilders.post("/api/games/{gameId}/players", GAME_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\":\"Ulisses\"}"))
                .andExpect(status().isNotFound());
        
        verify(gameService).enterGame(GAME_ID, "Ulisses");
    }
    
    @Test
    void testEnterGame_409() throws Exception {
        when(gameService.enterGame(any(), any())).thenThrow(new GameError.FullCapacityException());
        
        mvc.perform(MockMvcRequestBuilders.post("/api/games/{gameId}/players", GAME_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\":\"Ulisses\"}"))
                .andExpect(status().isConflict());
        
        verify(gameService).enterGame(GAME_ID, "Ulisses");
    }
    
    @Test
    void testLeaveGame_204() throws Exception {
        doNothing().when(gameService).leaveGame(any(), any());
        
        mvc.perform(MockMvcRequestBuilders.delete("/api/games/{gameId}/players/{playerId}", GAME_ID, PLAYER_ID))
                .andExpect(status().isNoContent());
        verify(gameService).leaveGame(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testLeaveGame_404() throws Exception {
        doThrow(new GameError.NotFoundException(GAME_ID)).when(gameService).leaveGame(any(), any());
        
        mvc.perform(MockMvcRequestBuilders.delete("/api/games/{gameId}/players/{playerId}", GAME_ID, PLAYER_ID))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testSubmitProposition_204() throws Exception {
        doNothing().when(gameService).submitProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/proposition", ROUND_ID)
                        .content("{\"gaps\":[\"one\"]}")
                        .header(HEADER_PLAYER, PLAYER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(gameService).submitProposition(eq(ROUND_ID), eq(PLAYER_ID), eq(List.of("one")));
    }
    
    @Test
    void testSubmitProposition_404_game() throws Exception {
        doThrow(new GameError.NotFoundException(GAME_ID)).when(gameService).submitProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/proposition", ROUND_ID)
                        .content("{\"gaps\":[\"one\"]}")
                        .header(HEADER_PLAYER, PLAYER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(gameService).submitProposition(eq(ROUND_ID), eq(PLAYER_ID), eq(List.of("one")));
    }
    
    @Test
    void testSubmitProposition_404_round() throws Exception {
        doThrow(new RoundError.NotFoundException(ROUND_ID)).when(gameService).submitProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/proposition", ROUND_ID)
                        .content("{\"gaps\":[\"one\"]}")
                        .header(HEADER_PLAYER, PLAYER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(gameService).submitProposition(eq(ROUND_ID), eq(PLAYER_ID), eq(List.of("one")));
    }
    
    @Test
    void testSubmitProposition_404() throws Exception {
        doThrow(new PlayerError.NotFoundException(PLAYER_ID)).when(gameService).submitProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/proposition", ROUND_ID)
                        .content("{\"gaps\":[\"one\"]}")
                        .header(HEADER_PLAYER, PLAYER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(gameService).submitProposition(eq(ROUND_ID), eq(PLAYER_ID), eq(List.of("one")));
    }
    
    @Test
    void testSelectProposition_204() throws Exception {
        doNothing().when(gameService).selectProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/proposition/{propositionId}", ROUND_ID, PROPOSITION_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNoContent());
        
        verify(gameService).selectProposition(ROUND_ID, PLAYER_ID, PROPOSITION_ID);
    }
    
    @Test
    void testSelectProposition_404_game() throws Exception {
        doThrow(new GameError.NotFoundException(GAME_ID)).when(gameService).selectProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/proposition/{propositionId}", ROUND_ID, PROPOSITION_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(gameService).selectProposition(ROUND_ID, PLAYER_ID, PROPOSITION_ID);
    }
    
    @Test
    void testSelectProposition_404_round() throws Exception {
        doThrow(new RoundError.NotFoundException(ROUND_ID)).when(gameService).selectProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/proposition/{propositionId}", ROUND_ID, PROPOSITION_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(gameService).selectProposition(ROUND_ID, PLAYER_ID, PROPOSITION_ID);
    }
    
    @Test
    void testSelectProposition_404_player() throws Exception {
        doThrow(new GameError.NotFoundException(GAME_ID)).when(gameService).selectProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/proposition/{propositionId}", ROUND_ID, PROPOSITION_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testGetRound_200() throws Exception {
        when(gameService.getRound(any(), any())).then(o -> new Round(ROUND_ID, PROMPT));
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + ROUND_ID + "\",\"prompt\":\"prompt\"}"));
        verify(gameService).getRound(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testGetRound_404() throws Exception {
        when(gameService.getRound(any(), any())).thenThrow(new GameError.NotFoundException(GAME_ID));
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(gameService).getRound(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testGetRound_400() throws Exception {
        when(gameService.getRound(any(), any())).thenThrow(new RoundError.OngoingException());
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isBadRequest());
        verify(gameService).getRound(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testGetRound_425() throws Exception {
        when(gameService.getRound(any(), any())).thenThrow(new GameError.NotEnoughPlayersException());
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isTooEarly());
        verify(gameService).getRound(GAME_ID, PLAYER_ID);
    }
}