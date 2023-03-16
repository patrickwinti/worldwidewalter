package ch.zhaw.www.controller;


import ch.zhaw.www.dto.*;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.model.Round;
import ch.zhaw.www.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for "games" resource.
 **/
@RequestMapping("/api")
@RestController
@Validated
@CrossOrigin(origins = "http://localhost:4200")
public class GameController {
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
        return ResponseEntity.ok(new GameDto(newGame.getId(), GameDto.State.WAITING_FOR_PLAYERS));
    }


    @Operation(summary = "Reads the status of th game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Existing game"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping(value = "/games/{gameId}", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GameDto> getGame(@PathVariable String gameId) {
        Game exitingGame = gameService.getGame(gameId);
        logger.log(Level.INFO, "game found {0}", exitingGame);
        return ResponseEntity.ok(new GameDto(gameId, GameDto.State.WAITING_FOR_PLAYERS));
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
        Player player = gameService.enterGame(gameId, playerDto.getPlayerName());
        logger.log(Level.INFO, "entered game {0}", player);
        return ResponseEntity.ok(new PlayerDto(player.getId()));
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
    //endregion

    //region Round endpoints
    @Operation(summary = "Player submits propositions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Proposition submitted to round"),
            @ApiResponse(responseCode = "400", description = "Missing proposition"),
            @ApiResponse(responseCode = "404", description = "Either game, round or player has not been found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/rounds/{roundId}/proposition", consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitProposition(@PathVariable String roundId, @Valid @RequestHeader("X-PLAYER-ID") String playerId, @Valid @RequestBody PropositionSubmissionDto proposition) {
        gameService.submitProposition(roundId, playerId, proposition.getGaps());
        logger.log(Level.INFO, "proposition submitted successfully");
    }

    @Operation(summary = "Player selects proposition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Proposition choice saved"),
            @ApiResponse(responseCode = "404", description = "Either game, round, proposition or player has not been found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/rounds/{roundId}/proposition/{propositionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void selectProposition(@PathVariable String roundId, @PathVariable String propositionId, @Valid @RequestHeader("X-PLAYER-ID") String playerId) {
        gameService.selectProposition(roundId, playerId, propositionId);
        logger.log(Level.INFO, "proposition selected successfully");
    }
    //endregion

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
        return ResponseEntity.ok(new RoundDto(round.getId(), round.getPromt()));
    }
}
