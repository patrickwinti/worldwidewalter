package ch.zhaw.www.controller;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.service.GameError;
import ch.zhaw.www.service.PlayerError;
import ch.zhaw.www.service.RoundError;
import ch.zhaw.www.service.RoundService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
class RoundControllerTest {
    private static final String PROPOSITION_ID = "-0-";
    private static final String GAME_ID = "123";
    private static final String PLAYER_ID = "456";
    private static final String ROUND_ID = "789";
    private static final String HEADER_PLAYER = "X-PLAYER-ID";
    
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private RoundService roundService;
    
    @AfterEach
    void tearDown() {
        disableFixedClocked();
    }
    
    @Test
    void testSubmitProposition_204() throws Exception {
        doNothing().when(roundService).submitProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .content("{\"gaps\":[\"one\"]}")
                        .header(HEADER_PLAYER, PLAYER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(roundService).submitProposition(ROUND_ID, PLAYER_ID, List.of("one"));
    }
    
    @Test
    void testSubmitProposition_404_round() throws Exception {
        doThrow(new RoundError.NotFoundException(ROUND_ID)).when(roundService).submitProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .content("{\"gaps\":[\"one\"]}")
                        .header(HEADER_PLAYER, PLAYER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(roundService).submitProposition(ROUND_ID, PLAYER_ID, List.of("one"));
    }
    
    @Test
    void testSelectProposition_204() throws Exception {
        doNothing().when(roundService).selectProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/propositions/{propositionId}", ROUND_ID, PROPOSITION_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNoContent());
        
        verify(roundService).selectProposition(ROUND_ID, PLAYER_ID, PROPOSITION_ID);
    }
    
    @Test
    void testSelectProposition_404_round() throws Exception {
        doThrow(new RoundError.NotFoundException(ROUND_ID)).when(roundService).selectProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/propositions/{propositionId}", ROUND_ID, PROPOSITION_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(roundService).selectProposition(ROUND_ID, PLAYER_ID, PROPOSITION_ID);
    }
    
    @Test
    void testSelectProposition_404_player() throws Exception {
        doThrow(new GameError.NotFoundException(GAME_ID)).when(roundService).selectProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/propositions/{propositionId}", ROUND_ID, PROPOSITION_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testSubmitProposition_404() throws Exception {
        doThrow(new PlayerError.NotFoundException(PLAYER_ID)).when(roundService).submitProposition(any(), any(), any());
        mvc.perform(MockMvcRequestBuilders.post("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .content("{\"gaps\":[\"one\"]}")
                        .header(HEADER_PLAYER, PLAYER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(roundService).submitProposition(ROUND_ID, PLAYER_ID, List.of("one"));
    }
    
    @Test
    void testGetAllPropositionForRound_200() throws Exception {
        String expectedDate = getExpectedDateInTheFuture(DEFAULT_PROPOSITION_DURATION.plus(DEFAULT_SUBMISSION_DURATION));
        var round = createRound();
        round.setSphinx(createPlayer());
        
        round.addProposition(createProposition("1", "prop 1"));
        round.addProposition(createProposition(PLAYER_ID, "prop 2", "prop 3"));
        List<Proposition> propositions = round.getPropositions();
        when(roundService.getRoundReadyForSelections(any(), any())).thenReturn(round);
        mvc.perform(MockMvcRequestBuilders.get("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("{\"roundId\":\"%s\",\"propositions\":" +
                                "[{\"id\":\"%s\",\"gaps\":[\"prop 1\"],\"readOnly\":false},{\"id\":\"%s\"," +
                                "\"gaps\":[\"prop 2\",\"prop 3\"],\"readOnly\":true}]," +
                                "\"selectionSubmissionEndInUtc\":\"%s\"}",
                        ROUND_ID, propositions.get(0).getId(), propositions.get(1).getId(), expectedDate)));
        verify(roundService).getRoundReadyForSelections(ROUND_ID, PLAYER_ID);
    }
    
    @Test
    void testGetAllPropositionForRound_200_sphinx() throws Exception {
        String expectedDate = getExpectedDateInTheFuture(DEFAULT_PROPOSITION_DURATION.plus(DEFAULT_SUBMISSION_DURATION));
        var round = createRound();
        Player sphinx = createPlayer();
        round.setSphinx(sphinx);
        
        round.addProposition(createProposition("1", "prop 1"));
        round.addProposition(createProposition(PLAYER_ID, "prop 2", "prop 3"));
        List<Proposition> propositions = round.getPropositions();
        when(roundService.getRoundReadyForSelections(any(), any())).thenReturn(round);
        mvc.perform(MockMvcRequestBuilders.get("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .header(HEADER_PLAYER, sphinx.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(String.format("{\"roundId\":\"%s\",\"propositions\":" +
                                "[{\"id\":\"%s\",\"gaps\":[\"prop 1\"],\"readOnly\":true},{\"id\":\"%s\"," +
                                "\"gaps\":[\"prop 2\",\"prop 3\"],\"readOnly\":true}]," +
                                "\"selectionSubmissionEndInUtc\":\"%s\"}",
                        ROUND_ID, propositions.get(0).getId(), propositions.get(1).getId(), expectedDate)));
        verify(roundService).getRoundReadyForSelections(ROUND_ID, sphinx.getId());
    }
    
    @Test
    void testGetAllPropositionForRound_404_round() throws Exception {
        doThrow(new RoundError.NotFoundException(ROUND_ID)).when(roundService).getRoundReadyForSelections(any(), any());
        mvc.perform(MockMvcRequestBuilders.get("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(roundService).getRoundReadyForSelections(ROUND_ID, PLAYER_ID);
    }
    
    @Test
    void testGetAllPropositionForRound_404_player() throws Exception {
        doThrow(new PlayerError.NotFoundException(ROUND_ID)).when(roundService).getRoundReadyForSelections(any(), any());
        mvc.perform(MockMvcRequestBuilders.get("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(roundService).getRoundReadyForSelections(ROUND_ID, PLAYER_ID);
    }
    
    @Test
    void testGetAllPropositionForRound_425() throws Exception {
        doThrow(new RoundError.IllegalStateException()).when(roundService).getRoundReadyForSelections(any(), any());
        mvc.perform(MockMvcRequestBuilders.get("/api/rounds/{roundId}/propositions", ROUND_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isTooEarly());
        verify(roundService).getRoundReadyForSelections(ROUND_ID, PLAYER_ID);
    }
    
    @Test
    void testFetchResults_200() throws Exception {
        var round = mock(Round.class);
        var game = mock(Game.class);
        var p1Id = "p1Id";
        var p2Id = "p2Id";
        var p1Name = "player1";
        var p2Name = "player2";
        
        var p1prop = "prop1";
        var p2prop = "prop2";
        var p1propId = "p1propId";
        var p2propId = "p2propId";
        var propositions = new ArrayList<Proposition>();
        var prop1 = new Proposition(p1propId, Collections.singletonList(p1prop));
        prop1.submittedBy(p1Id);
        var prop2 = new Proposition(p2propId, Collections.singletonList(p2prop));
        prop2.submittedBy(p2Id);
        propositions.add(prop1);
        propositions.add(prop2);
        
        var points = Map.of(p1Id, 3, p2Id, 1);
        var selections = Map.of(p2Id, p1propId);
        
        when(roundService.getRoundClosedForSelections(any(), any())).thenReturn(round);
        when(roundService.getGameForRound(any())).thenReturn(game);
        when(game.getPoints()).thenReturn(points);
        when(round.getSelections()).thenReturn(selections);
        when(round.getPropositions()).thenReturn(propositions);
        when(game.getPlayerNameFromId(p1Id)).thenReturn(p1Name);
        when(game.getPlayerNameFromId(p2Id)).thenReturn(p2Name);
        
        mvc.perform(MockMvcRequestBuilders.get("/api/rounds/{roundId}/results", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"ranking\":[{\"playerName\":\"player1\",\"points\":3},{\"playerName\":\"player2\",\"points\":1}]," +
                                "\"selections\":[{\"authors\":[\"player1\"],\"gaps\":[\"prop1\"],\"selectors\":[\"player2\"]},{\"authors\":[\"player2\"],\"gaps\":[\"prop2\"],\"selectors\":[]}]}"));
        
        verify(roundService).getRoundClosedForSelections(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testFetchResults_404_game() throws Exception {
        doThrow(new GameError.NotFoundException(ROUND_ID)).when(roundService).getRoundClosedForSelections(any(), any());
        mvc.perform(MockMvcRequestBuilders.get("/api/rounds/{roundId}/results", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(roundService).getRoundClosedForSelections(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testFetchResults_425() throws Exception {
        doThrow(new RoundError.IllegalStateException()).when(roundService).getRoundClosedForSelections(any(), any());
        mvc.perform(MockMvcRequestBuilders.get("/api/rounds/{roundId}/results", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isTooEarly());
        verify(roundService).getRoundClosedForSelections(GAME_ID, PLAYER_ID);
    }
    
    private static String getExpectedDateInTheFuture(Duration duration) {
        enableFixedClocked();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("UTC")).format(getFixedClockInstant().plus(duration));
    }
}