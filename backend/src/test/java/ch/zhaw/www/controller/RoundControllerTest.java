package ch.zhaw.www.controller;

import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.service.GameError;
import ch.zhaw.www.service.PlayerError;
import ch.zhaw.www.service.RoundError;
import ch.zhaw.www.service.RoundService;
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
import java.util.List;

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
    @MockBean
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
    
    private static String getExpectedDateInTheFuture(Duration duration) {
        enableFixedClocked();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("UTC")).format(getFixedClockInstant().plus(duration));
    }
}