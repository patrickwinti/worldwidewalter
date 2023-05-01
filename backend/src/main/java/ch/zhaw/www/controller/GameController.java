package ch.zhaw.www.controller;

import ch.zhaw.www.dto.*;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.service.GameService;
import ch.zhaw.www.service.RoundError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for "games" resource.
 **/

@RequestMapping("/api")
@RestController
@Validated
@CrossOrigin({"http://localhost:4200", "http://worldwidewalter.ch"})
public class GameController {
    private final Logger logger = Logger.getLogger(GameController.class.getSimpleName());
    private final GameService gameService;
    
    GameController(GameService gameService) {
        this.gameService = gameService;
    }
    
    @Operation(summary = "Creates a new game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game created"),
            @ApiResponse(responseCode = "409", description = "Game exists with that ID"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/games", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GameDto> createGame() {
        Game newGame = gameService.createGame();
        logger.log(Level.INFO, "created game {0}", newGame.getId());
        return ResponseEntity.ok(new GameDto(newGame.getId()));
    }
    
    //region Game-Player endpoints
    @Operation(summary = "Player joins an existing game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player has joined the game"),
            @ApiResponse(responseCode = "400", description = "Missing player name"),
            @ApiResponse(responseCode = "404", description = "Game has not been found"),
            @ApiResponse(responseCode = "409", description = "Game has reached max capacity"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/games/{gameId}/players", produces = "application/json", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PlayerDto> enterGame(@PathVariable String gameId, @Valid @RequestBody PlayerJoinRequestDto playerDto) {
        String playerId = gameService.enterGame(gameId, playerDto.getPlayerName());
        logger.log(Level.INFO, () -> String.format("%s entered game %s", playerId, gameId));
        return ResponseEntity.ok(new PlayerDto(playerId));
    }
    
    @Operation(summary = "Player leaves game gracefully")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Player left game"),
            @ApiResponse(responseCode = "404", description = "Game has not been found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @DeleteMapping(value = "/games/{gameId}/players/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGame(@PathVariable String gameId, @Valid @PathVariable String playerId) {
        gameService.leaveGame(gameId, playerId);
        logger.log(Level.INFO, "left game successfully");
    }
    
    @Operation(summary = "Retrieves the points for each player")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Results table with player names and their point value"),
            @ApiResponse(responseCode = "404", description = "Game has not been found"),
            @ApiResponse(responseCode = "405", description = "Not all selections have been submitted yet"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping(value = "/games/{gameId}/results", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResultDto> fetchResultsForRound(@PathVariable String gameId, @Valid @RequestHeader("X-PLAYER-ID") String playerId) {
        var round = gameService.getRoundClosedForSelections(gameId, playerId);
        var game = gameService.getGame(gameId);
        
        if (round == null) {
            throw new RoundError.NotFoundException("not current round for game " + gameId);
        }
        var propositions = round.getPropositions();
        var selections = round.getSelections();
        
        List<SelectionDto> selectionDtos = createSelectionDtos(game, propositions, selections);
        
        var resultDto = new ResultDto(
                game.getPoints()
                        .entrySet()
                        .stream()
                        .map(entry -> new RankingDto(game.getPlayerNameFromId(entry.getKey()), entry.getValue()))
                        .toList(),
                selectionDtos);
        
        logger.log(Level.INFO, "game results returned {0}", game);
        return ResponseEntity.ok(resultDto);
    }
    
    @Operation(summary = "Player requested current round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New round started"),
            @ApiResponse(responseCode = "404", description = "Game has not been found"),
            @ApiResponse(responseCode = "425", description = "Game has not enough players to continue"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping(value = "/games/{gameId}/rounds", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RoundDto> getRound(@PathVariable String gameId, @Valid @RequestHeader("X-PLAYER-ID") String playerId) {
        var round = gameService.getRoundOpenForPropositions(gameId, playerId);
        logger.log(Level.INFO, "get current round {0}", round);
        
        PlayerDto sphinx = null;
        if (round.getSphinx() != null) {
            if (round.getSphinx().getId().equals(playerId)) {
                logger.log(Level.INFO, "current sphinx requesting round {0}", playerId);
                sphinx = new PlayerDto(round.getSphinx().getId());
            } else {
                // for security reasons only pass the sphinx id to the actual Sphinx
                sphinx = new PlayerDto("");
            }
            sphinx.setPlayerName(round.getSphinx().getName());
        }
        
        return ResponseEntity.ok(new RoundDto(round.getId(), round.getPrompt().getStatement(), round.getPrompt().getNumberOfPlaceholders(), sphinx, round.getPropositionSubmissionEnd()));
    }
    
    @Operation(summary = "Player requested to enter round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "New round started, added to round or acknowledge as part of game"),
            @ApiResponse(responseCode = "404", description = "Game has not been found"),
            @ApiResponse(responseCode = "409", description = "Game is at capacity"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PutMapping(value = "/games/{gameId}/rounds")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enterRound(@PathVariable String gameId, @Valid @RequestHeader("X-PLAYER-ID") String playerId) {
        gameService.enterRound(gameId, playerId);
        logger.log(Level.INFO, "{0} will participate next round", playerId);
    }
    
    @Operation(summary = "Player leaves game by destroying webapp")
    @PostMapping(value = "/games/{gameId}/players/{playerId}")
    public void leaveGameAfterDestruction(@PathVariable String gameId, @Valid @PathVariable String playerId) {
        gameService.leaveGame(gameId, playerId);
        logger.log(Level.INFO, "left game ungracefully by destroying webapp");
    }
    
    private static List<SelectionDto> createSelectionDtos(Game game, List<Proposition> propositions, Map<String, String> selections) {
        return propositions.stream()
                .map(proposition ->
                        new SelectionDto(
                                proposition.getPlayerIds().stream().map(game::getPlayerNameFromId).toList(),
                                proposition.getGaps(),
                                selections.entrySet().stream()
                                        .filter(entry -> entry.getValue().equals(proposition.getId()))
                                        .map(entry -> game.getPlayerNameFromId(entry.getKey()))
                                        .toList()
                        )
                )
                .toList();
    }
}
