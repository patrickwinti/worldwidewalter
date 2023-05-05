package ch.zhaw.www.controller;

import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.model.Round;
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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
class GameControllerTest {
    private static final String GAME_ID = "123";
    private static final String PLAYER_ID = "456";
    private static final String ROUND_ID = "789";
    private static final String HEADER_PLAYER = "X-PLAYER-ID";
    
    @Autowired
    private MockMvc mvc;
    @MockBean
    private GameService gameService;
    
    private static String getExpectedDateInTheFuture() {
        enableFixedClocked();
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("UTC")).format(getFixedClockInstant().plus(ch.zhaw.www.TestHelper.DEFAULT_PROPOSITION_DURATION));
    }
    
    @AfterEach
    void tearDown() {
        disableFixedClocked();
    }
    
    @Test
    void testEnterGame_200() throws Exception {
        Game game = mock(Game.class);
        Player player = new Player(PLAYER_ID, "Ulisses");
        
        when(gameService.enterGame(any(), any())).thenReturn(player);
        when(gameService.getGame(any())).thenReturn(game);
        
        mvc.perform(MockMvcRequestBuilders.post("/api/games/{gameId}/players", GAME_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerName\":\"Ulisses\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + PLAYER_ID + "\", \"playerName\":\"Ulisses\"}"));
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
        when(gameService.getRoundOpenForPropositions(any(), any())).thenReturn(round);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + round.getId() + "\",\"prompt\":\"I am WALTER\"}"));
        verify(gameService).getRoundOpenForPropositions(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testGetRound_200_withSphinx_sphinxPlayer() throws Exception {
        var round = createRound();
        var sphinx = createPlayer("Sphinx");
        round.setSphinx(sphinx);
        when(gameService.getRoundOpenForPropositions(any(), any())).thenReturn(round);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, sphinx.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + round.getId() + "\",\"prompt\":\"I am WALTER\",\"sphinx\":{\"id\":\"" + sphinx.getId() + "\",\"playerName\":\"Sphinx\"}}"));
        verify(gameService).getRoundOpenForPropositions(GAME_ID, sphinx.getId());
    }
    
    @Test
    void testGetRound_200_withSphinx_otherPlayer() throws Exception {
        var round = createRound();
        var sphinx = createPlayer("Sphinx");
        round.setSphinx(sphinx);
        when(gameService.getRoundOpenForPropositions(any(), any())).thenReturn(round);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + round.getId() + "\",\"prompt\":\"I am WALTER\",\"sphinx\":{\"playerName\":\"Sphinx\"}}"));
        verify(gameService).getRoundOpenForPropositions(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testGetRound_200_withDate() throws Exception {
        String expectedDate = getExpectedDateInTheFuture();
        var round = createRound();
        round.setSphinx(createPlayer("Sphinx"));
        when(gameService.getRoundOpenForPropositions(any(), any())).thenReturn(round);
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":\"" + round.getId() + "\",\"prompt\":\"I am WALTER\",\"endOfSubmissionsInUtc\":\"" + expectedDate + "\"}"));
        verify(gameService).getRoundOpenForPropositions(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testGetRound_425() throws Exception {
        when(gameService.getRoundOpenForPropositions(any(), any())).thenThrow(new RoundError.IllegalStateException());
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isTooEarly());
        verify(gameService).getRoundOpenForPropositions(GAME_ID, PLAYER_ID);
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
    void enterRound_403_game() throws Exception {
        doThrow(new RoundError.IllegalOperationException()).when(gameService).enterRound(any(), any());
        mvc.perform(MockMvcRequestBuilders.put("/api/games/{gameId}/rounds", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isForbidden());
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
        var round = mock(Round.class);
        var game = mock(Game.class);
        var p1Name = "player1";
        var p2Name = "player2";
        var p1 = createPlayer(p1Name);
        var p2 = createPlayer(p2Name);
        
        var p1prop = "prop1";
        var p2prop = "prop2";
        var p1propId = "p1propId";
        var p2propId = "p2propId";
        var prop1 = new Proposition(p1propId, List.of(p1prop));
        prop1.submittedBy(p1.getId());
        var prop2 = new Proposition(p2propId, List.of(p2prop));
        prop2.submittedBy(p2.getId());
        var propositions = List.of(prop1, prop2);
        
        var points = Map.of(p1, 3, p2, 1);
        var selections = Map.of(p2.getId(), p1propId);
        
        when(gameService.getRoundClosedForSelections(any(), any())).thenReturn(round);
        when(gameService.getGame(any())).thenReturn(game);
        when(game.getPoints()).thenReturn(points);
        when(round.getSelections()).thenReturn(selections);
        when(round.getPropositions()).thenReturn(propositions);
        when(game.getPlayerNameFromId(p1.getId())).thenReturn(p1Name);
        when(game.getPlayerNameFromId(p2.getId())).thenReturn(p2Name);
        
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/results", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"ranking\":[{\"playerName\":\"player1\",\"points\":3},{\"playerName\":\"player2\",\"points\":1}]," +
                                "\"selections\":[{\"authors\":[\"player1\"],\"gaps\":[\"prop1\"],\"selectors\":[\"player2\"]},{\"authors\":[\"player2\"],\"gaps\":[\"prop2\"],\"selectors\":[]}]}"));
        
        verify(gameService).getRoundClosedForSelections(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testFetchResults_404_game() throws Exception {
        doThrow(new GameError.NotFoundException(ROUND_ID)).when(gameService).getRoundClosedForSelections(any(), any());
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/results", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isNotFound());
        verify(gameService).getRoundClosedForSelections(GAME_ID, PLAYER_ID);
    }
    
    @Test
    void testFetchResults_425() throws Exception {
        doThrow(new RoundError.IllegalStateException()).when(gameService).getRoundClosedForSelections(any(), any());
        mvc.perform(MockMvcRequestBuilders.get("/api/games/{gameId}/results", GAME_ID)
                        .header(HEADER_PLAYER, PLAYER_ID))
                .andExpect(status().isTooEarly());
        verify(gameService).getRoundClosedForSelections(GAME_ID, PLAYER_ID);
    }
}