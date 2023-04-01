package ch.zhaw.www.controller;

import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.service.GameError;
import ch.zhaw.www.service.GameService;
import ch.zhaw.www.service.PlayerError;
import ch.zhaw.www.service.RoundError;
import ch.zhaw.www.utils.InstantWrapper;
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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ch.zhaw.www.TestHelper.*;
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
    private static final String PROPOSITION_ID = "-0-";
    private static final String HEADER_PLAYER = "X-PLAYER-ID";
    
    @Autowired
    private MockMvc mvc;
    @MockBean
    private GameService gameService;
    
    private static String getExpectedDateInTheFuture(Duration duration) {
        Instant i = Instant.now();
        InstantWrapper.clock = Clock.fixed(i, ZoneId.systemDefault());
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("UTC")).format(i.plus(duration));
    }
    
    @Test
    void testEnterGame_200() throws Exception {
        when(gameService.enterGame(any(), any())).then(m -> new Player(PLAYER_ID, "Ulisses"));
        
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
        when(gameService.createGame()).then(invocationOnMock -> createGame(GAME_ID));
        mvc.perform(MockMvcRequestBuilders.post("/api/games"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + GAME_ID + "\"}"));
    }
    
    @Test
    void testSubmitProposition_204() throws Exception {
        doNothing().when(gameService).submitProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .content("{\"gaps\":[\"one\"]}")
                        .header(HEADER_PLAYER, PLAYER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(gameService).submitProposition(ROUND_ID, PLAYER_ID, List.of("one"));
    }
    
    @Test
    void testSubmitProposition_404_round() throws Exception {
        doThrow(new RoundError.NotFoundException(ROUND_ID)).when(gameService).submitProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .content("{\"gaps\":[\"one\"]}")
                        .header(HEADER_PLAYER, PLAYER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(gameService).submitProposition(ROUND_ID, PLAYER_ID, List.of("one"));
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
    void testSubmitProposition_404() throws Exception {
        doThrow(new PlayerError.NotFoundException(PLAYER_ID)).when(gameService).submitProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .content("{\"gaps\":[\"one\"]}")
                        .header(HEADER_PLAYER, PLAYER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(gameService).submitProposition(ROUND_ID, PLAYER_ID, List.of("one"));
    }
    
    @Test
    void testGetRound_200() throws Exception {
        Round round = createRound();
        when(gameService.getCurrentRoundInGame(any(), any())).then(o -> round);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + round.getId() + "\",\"prompt\":\"I am WALTER\"}"));
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
    void testGetRound_200_withDate() throws Exception {
        String expectedDate = getExpectedDateInTheFuture(DEFAULT_PROPOSITION_DURATION);
        Round round = createRound();
        round.setSphinx(createPlayer());
        when(gameService.getCurrentRoundInGame(any(), any())).then(o -> round);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + round.getId() + "\",\"prompt\":\"I am WALTER\",\"endOfSubmissionsInUtc\":\"" + expectedDate + "\"}"));
        verify(gameService).getCurrentRoundInGame(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void fetchResults_200() throws Exception {
        var game = createGame();
        Round round = createRound();
        game.addRound(round);
        when(gameService.getGame(any())).thenReturn(game);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/results", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk());
        verify(gameService).getGame(GAME_ID);
    }
    
    @Test
    void fetchResults_404_game() throws Exception {
        doThrow(new GameError.NotFoundException(ROUND_ID)).when(gameService).getGame(any());
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/results", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(gameService).getGame(GAME_ID);
    }
    
    @Test
    void getAllPropositionForRound_200() throws Exception {
        String expectedDate = getExpectedDateInTheFuture(DEFAULT_PROPOSITION_DURATION.plus(DEFAULT_SUBMISSION_DURATION));
        var round = createRound();
        round.setSphinx(createPlayer());
        round.addProposition("1", List.of("prop 1"));
        round.addProposition("2", List.of("prop 2", "prop 3"));
        when(gameService.getRound(any(), any())).thenReturn(round);
        mvc.perform(MockMvcRequestBuilders.get("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("{\"roundId\":\"%s\",\"propositions\":{\"1\":[\"prop 1\"],\"2\":[\"prop 2\",\"prop 3\"]},\"selectionSubmissionEndInUtc\":\"%s\"}", ROUND_ID, expectedDate)));
        verify(gameService).getRound(ROUND_ID, PLAYER_ID);
    }
    
    @Test
    void getAllPropositionForRound_404_round() throws Exception {
        doThrow(new RoundError.NotFoundException(ROUND_ID)).when(gameService).getRound(any(), any());
        mvc.perform(MockMvcRequestBuilders.get("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(gameService).getRound(ROUND_ID, PLAYER_ID);
    }
    
    @Test
    void getAllPropositionForRound_404_player() throws Exception {
        doThrow(new PlayerError.NotFoundException(ROUND_ID)).when(gameService).getRound(any(), any());
        mvc.perform(MockMvcRequestBuilders.get("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(gameService).getRound(ROUND_ID, PLAYER_ID);
    }
}