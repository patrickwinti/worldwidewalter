package ch.zhaw.www.controller;


import ch.zhaw.www.dto.*;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for "games" resource.
 **/
@RequestMapping("/api")
@RestController
@Validated
public class GameController {
    private static final String JOIN_DELIMITER = " - ";
    private static final String TIME_OUT_RESPONSE = "Time Out.";
    private static final long TIME_OUT_IN_MS = 100_000L;
    private final Logger logger = Logger.getLogger(GameController.class.getSimpleName());
    private final GameService gameService;


    GameController(GameService gameService) {
        this.gameService = gameService;
    }


    @Operation(summary = "Creates a new game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game created"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/games", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GameDto> createGame() {
        Game newGame = gameService.createGame();
        logger.log(Level.INFO, "created game {0}", newGame);
        return ResponseEntity.ok(new GameDto(newGame.getId(), String.format("/api/games/%s", newGame.getId())));
    }

    //region Game-Player endpoints
    @Operation(summary = "Player joins an existing game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player has joined the game"),
            @ApiResponse(responseCode = "404", description = "Game has not been found"),
            @ApiResponse(responseCode = "409", description = "Game has reached max capacity"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/games/{gameId}/players", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PlayerDto> enterGame(@PathVariable String gameId, @Valid @RequestBody PlayerJoinDto playerDto) {
        Player player = gameService.enterGame(gameId, playerDto.getPlayerName());
        logger.info("entered game " + player);
        return ResponseEntity.ok(new PlayerDto(player.getId()));
    }

    @Operation(summary = "Player leaves game gracefully")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Player left game"),
            @ApiResponse(responseCode = "404", description = "Game has not been found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @DeleteMapping(value = "/games/{gameId}/players/{playerId}", produces = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGame(@PathVariable String gameId, @Valid @PathVariable String playerId) {
        gameService.leaveGame(gameId, playerId);
        logger.log(Level.INFO, "left game successfully");
    }
    //endregion

    @Operation(summary = "Player submits propositions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proposition submitted to round"),
            @ApiResponse(responseCode = "404", description = "Either game, round or player has not been found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/rounds/{roundId}/proposition", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public void submitProposition(@PathVariable String roundId, @RequestHeader("X-PLAYER-ID") String playerId, @Valid @RequestBody PropositionSubmissionDto proposition) {
        gameService.submitProposition(roundId, playerId, String.join(JOIN_DELIMITER, proposition.getGaps()));
        logger.log(Level.INFO, "proposition submitted successfully");
    }

    @Operation(summary = "Player selects proposition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Proposition choice saved"),
            @ApiResponse(responseCode = "404", description = "Either game, round, proposition or player has not been found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/rounds/{roundId}/proposition/{propositionId}", produces = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void selectProposition(@PathVariable String roundId, @PathVariable String propositionId, @RequestHeader("X-PLAYER-ID") String playerId) {
        gameService.selectProposition(roundId, playerId, propositionId);
        logger.log(Level.INFO, "proposition selected successfully");
    }

    @Operation(summary = "Player requested to start new round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "New round started"),
            @ApiResponse(responseCode = "400", description = "Game is in an invalid state to start new round"),
            @ApiResponse(responseCode = "404", description = "Game has not been found"),
            @ApiResponse(responseCode = "409", description = "Game has not enough players to continue"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/games/{gameId}/rounds", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<RoundDto> startNextRound(@PathVariable String gameId) {
        Round round = gameService.startNextRound(gameId);
        logger.log(Level.INFO, "started next round {0}", round);
        return ResponseEntity.ok(new RoundDto(round.getId()));
    }
}
