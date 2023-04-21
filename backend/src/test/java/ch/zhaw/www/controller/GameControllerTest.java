package ch.zhaw.www.controller;

import ch.zhaw.www.service.GameError;
import ch.zhaw.www.service.GameService;
import ch.zhaw.www.service.PlayerError;
import ch.zhaw.www.service.RoundError;
import org.junit.jupiter.api.AfterEach;
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

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static ch.zhaw.www.TestHelper.*;
import static ch.zhaw.www.TimeHelper.*;
import static org.mockito.ArgumentMatchers.any;
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
    private static final String HEADER_PLAYER = "X-PLAYER-ID";
    
    @Autowired
    private MockMvc mvc;
    @MockBean
    private GameService gameService;
    
    private static String getExpectedDateInTheFuture(Duration duration) {
        enableFixedClocked();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("UTC")).format(getFixedClockInstant().plus(duration));
    }
    
    @AfterEach
    void tearDown() {
        disableFixedClocked();
    }
    
    @Test
    void testEnterGame_200() throws Exception {
        when(gameService.enterGame(any(), any())).thenReturn(PLAYER_ID);
        
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
    void testCreateGame_200() throws Exception {
        when(gameService.createGame()).thenReturn(createGame(GAME_ID));
        mvc.perform(MockMvcRequestBuilders.post("/api/games"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + GAME_ID + "\"}"));
        verify(gameService).createGame();
    }
    
    @Test
    void testCreateGame_409() throws Exception {
        doThrow(new GameError.ExistAlready()).when(gameService).createGame();
        mvc.perform(MockMvcRequestBuilders.post("/api/games"))
                .andExpect(status().isConflict());
        verify(gameService).createGame();
    }
    
    @Test
    void testGetRound_200_noSphinx() throws Exception {
        var round = createRound();
        when(gameService.getCurrentRoundInGame(any(), any())).thenReturn(round);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + round.getId() + "\",\"prompt\":\"I am WALTER\"}"));
        verify(gameService).getCurrentRoundInGame(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testGetRound_200_withSphinx_sphinxPlayer() throws Exception {
        var round = createRound();
        var sphinx = createPlayer("Sphinx");
        round.setSphinx(sphinx);
        when(gameService.getCurrentRoundInGame(any(), any())).thenReturn(round);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, sphinx.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + round.getId() + "\",\"prompt\":\"I am WALTER\",\"sphinx\":{\"id\":\"" + sphinx.getId() + "\",\"playerName\":\"Sphinx\"}}"));
        verify(gameService).getCurrentRoundInGame(GAME_ID, sphinx.getId());
    }
    
    @Test
    void testGetRound_200_withSphinx_otherPlayer() throws Exception {
        var round = createRound();
        var sphinx = createPlayer("Sphinx");
        round.setSphinx(sphinx);
        when(gameService.getCurrentRoundInGame(any(), any())).thenReturn(round);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + round.getId() + "\",\"prompt\":\"I am WALTER\",\"sphinx\":{\"id\":\"\",\"playerName\":\"Sphinx\"}}"));
        verify(gameService).getCurrentRoundInGame(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testGetRound_200_withDate() throws Exception {
        String expectedDate = getExpectedDateInTheFuture(DEFAULT_PROPOSITION_DURATION);
        var round = createRound();
        round.setSphinx(createPlayer("Sphinx"));
        when(gameService.getCurrentRoundInGame(any(), any())).thenReturn(round);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + round.getId() + "\",\"prompt\":\"I am WALTER\",\"endOfSubmissionsInUtc\":\"" + expectedDate + "\"}"));
        verify(gameService).getCurrentRoundInGame(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testGetRound_425() throws Exception {
        when(gameService.getCurrentRoundInGame(any(), any())).thenThrow(new RoundError.IllegalStateException());
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isTooEarly());
        verify(gameService).getCurrentRoundInGame(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void enterRound_204() throws Exception {
        doNothing().when(gameService).enterRound(any(), any());
        mvc.perform(MockMvcRequestBuilders.put("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNoContent());
        verify(gameService).enterRound(GAME_ID, PLAYER_ID);
        
    }
    
    @Test
    void enterRound_404_game() throws Exception {
        doThrow(new GameError.NotFoundException(GAME_ID)).when(gameService).enterRound(any(), any());
        mvc.perform(MockMvcRequestBuilders.put("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(gameService).enterRound(GAME_ID, PLAYER_ID);
        
    }
    
    @Test
    void enterRound_404_player() throws Exception {
        doThrow(new PlayerError.NotFoundException(GAME_ID)).when(gameService).enterRound(any(), any());
        mvc.perform(MockMvcRequestBuilders.put("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(gameService).enterRound(GAME_ID, PLAYER_ID);
        
    }
    
    @Test
    void enterRound_409() throws Exception {
        doThrow(new GameError.FullCapacityException()).when(gameService).enterRound(any(), any());
        mvc.perform(MockMvcRequestBuilders.put("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isConflict());
        verify(gameService).enterRound(GAME_ID, PLAYER_ID);
        
    }
    
    @Test
    void testFetchResults_200() throws Exception {
        var game = createGame();
        var round = createRound();
        game.addRound(round);
        when(gameService.getGame(any())).thenReturn(game);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/results", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk());
        verify(gameService).getGame(GAME_ID);
    }
    
    @Test
    void testFetchResults_404_game() throws Exception {
        doThrow(new GameError.NotFoundException(ROUND_ID)).when(gameService).getGame(any());
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/results", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(gameService).getGame(GAME_ID);
    }
    
}