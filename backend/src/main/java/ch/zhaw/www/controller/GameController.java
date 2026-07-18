package ch.zhaw.www.controller;

import ch.zhaw.www.dto.*;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
import ch.zhaw.www.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for "games" resource.
 **/
@RequestMapping("/api")
@RestController
@Validated
@CrossOrigin({"http://localhost:4200", "http://worldwidewalter.ch", "https://worldwidewalter.ch", "https://www.worldwidewalter.ch", "https://160.85.253.247", "http://160.85.253.247:8080"})
public class GameController {
    private final Logger logger = Logger.getLogger(GameController.class.getSimpleName());
    private final GameService gameService;
    
    GameController(GameService gameService) {
        this.gameService = gameService;
    }
    
    @Operation(summary = "Creates a new game with the creator as host")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game created"),
            @ApiResponse(responseCode = "400", description = "Missing host name"),
            @ApiResponse(responseCode = "409", description = "Game exists with that ID"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/games", produces = "application/json", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GameCreatedDto> createGame(@Valid @RequestBody PlayerJoinRequestDto playerDto) {
        Game newGame = gameService.createGame(playerDto.getPlayerName().trim());
        Player host = newGame.getHost().orElseThrow();
        logger.log(Level.INFO, "created game {0} with host {1}", new Object[]{newGame.getId(), host.getId()});
        return ResponseEntity.ok(new GameCreatedDto(newGame.getId(), new PlayerDto(host.getId(), host.getName())));
    }

    @Operation(summary = "Host starts the game so the first round can begin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Game started"),
            @ApiResponse(responseCode = "403", description = "Player is not the host"),
            @ApiResponse(responseCode = "404", description = "Game has not been found"),
            @ApiResponse(responseCode = "409", description = "Not enough players to start"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/games/{gameId}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startGame(@PathVariable String gameId, @Valid @RequestHeader("X-PLAYER-ID") String playerId) {
        gameService.startGame(gameId, playerId);
        logger.log(Level.INFO, "game {0} started by {1}", new Object[]{gameId, playerId});
    }

    @Operation(summary = "Returns the current lobby state (joined players, host, started flag)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current lobby state"),
            @ApiResponse(responseCode = "404", description = "Game has not been found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping(value = "/games/{gameId}/lobby", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LobbyDto> getLobby(@PathVariable String gameId) {
        return ResponseEntity.ok(LobbyDto.from(gameService.getGame(gameId)));
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
        Player player = gameService.enterGame(gameId, playerDto.getPlayerName().trim());
        logger.log(Level.INFO, () -> String.format("%s entered game %s", player, gameId));
        return ResponseEntity.ok(new PlayerDto(player.getId(), player.getName()));
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
    public ResponseEntity<ResultDto> fetchResultsForRound(@PathVariable String gameId) {
        var game = gameService.getGame(gameId);
        var resultDto = new ResultDto(
                game.getPoints()
                        .entrySet()
                        .stream()
                        .map(entry -> new RankingDto(game.getPlayerNameFromId(entry.getKey()), entry.getValue()))
                        .toList(),
                Collections.emptyList());
        
        logger.log(Level.INFO, "game results returned {0}", game.getId());
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
                sphinx = new PlayerDto(round.getSphinx().getId(), round.getSphinx().getName());
            } else {
                // for security reasons only pass the sphinx id to the actual Sphinx
                sphinx = new PlayerDto("", round.getSphinx().getName());
            }
        }
        
        return ResponseEntity.ok(new RoundDto(
                round.getId(),
                round.getPrompt().getStatement(),
                round.getPrompt().getWalters(),
                sphinx,
                round.getPropositionSubmissionEnd()));
    }
    
    @Operation(summary = "Player requested to enter round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "New round started, added to round or acknowledge as part of game"),
            @ApiResponse(responseCode = "403", description = "Player cannot enter this round because it's either already running"),
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

    @Operation(summary = "Player rejoins a game after disconnection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player rejoined the game"),
            @ApiResponse(responseCode = "404", description = "Game or player not found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/games/{gameId}/players/{playerId}/rejoin", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PlayerDto> rejoinGame(@PathVariable String gameId, @PathVariable String playerId) {
        Player player = gameService.rejoinGame(gameId, playerId);
        logger.log(Level.INFO, "Player {0} rejoined game {1}", new Object[]{playerId, gameId});
        return ResponseEntity.ok(new PlayerDto(player.getId(), player.getName()));
    }
}
