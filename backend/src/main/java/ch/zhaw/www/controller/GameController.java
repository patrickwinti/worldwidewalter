package ch.zhaw.www.controller;

import ch.zhaw.www.dto.*;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.service.GameService;
import ch.zhaw.www.service.RoundError;
import ch.zhaw.www.service.RoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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
    private final RoundService roundService;
    
    GameController(GameService gameService, RoundService roundService) {
        this.gameService = gameService;
        this.roundService = roundService;
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
        logger.log(Level.INFO, "created game {0}", newGame);
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
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping(value = "/games/{gameId}/results", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResultDto> fetchResultsForRound(@PathVariable String gameId) {
        var game = gameService.getGame(gameId);
        var currentRound = game.getCurrentRound();
        if(currentRound == null) {
            throw new RoundError.NotFoundException("not current round for game " + gameId);
        }
        var propositions = currentRound.getPropositions();

//        var selections = game.getCurrentRound().getSelections();
        var selections = new HashMap<String, String>();
        for (Proposition proposition: propositions) {
            selections.put(proposition.getPlayerId(), proposition.getId());
        }

        List<SelectionDto> selectionDtos = createSelectionDtos(game, propositions, selections);

        var resultDto = new ResultDto(
                Arrays.asList(new RankingDto("Elias", 10), new RankingDto("Jenny", 1), new RankingDto("Sara", 12)),
                selectionDtos);

        //TODO return valid results

        logger.log(Level.INFO, "game results returned {0}", game);
        return ResponseEntity.ok(resultDto);
    }
    //endregion
    
    //region Round endpoints
    @Operation(summary = "Player submits propositions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Proposition submitted to round"),
            @ApiResponse(responseCode = "400", description = "Missing proposition"),
            @ApiResponse(responseCode = "404", description = "Either round or player has not been found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/rounds/{roundId}/propositions", consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitProposition(@PathVariable String roundId, @Valid @RequestHeader("X-PLAYER-ID") String playerId, @Valid @RequestBody PropositionSubmissionDto proposition) {
        roundService.submitProposition(roundId, playerId, proposition.getGaps());
        logger.log(Level.INFO, "proposition submitted successfully");
    }
    
    @Operation(summary = "Player selects proposition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Proposition choice saved"),
            @ApiResponse(responseCode = "404", description = "Either round, proposition or player has not been found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/rounds/{roundId}/propositions/{propositionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void selectProposition(@PathVariable String roundId, @PathVariable String propositionId, @Valid @RequestHeader("X-PLAYER-ID") String playerId) {
        roundService.selectProposition(roundId, playerId, propositionId);
        logger.log(Level.INFO, "proposition selected successfully");
    }
    
    @Operation(summary = "Get all propositions sent by the players in current round")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propositions in round to be selected by players"),
            @ApiResponse(responseCode = "404", description = "Either round or player has not been found"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping(value = "/rounds/{roundId}/propositions", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PropositionSelectionDto> getAllPropositionForRound(@PathVariable String roundId, @Valid @RequestHeader("X-PLAYER-ID") String playerId) {
        var round = roundService.getRound(roundId, playerId);
        logger.log(Level.INFO, "round selections returned {0}", round);
        List<PropositionSelectionDto.Proposition> propositions = new ArrayList<>();
        var isSphinx = round.getSphinx() != null && round.getSphinx().getId().equals(playerId);
        round.getPropositions().forEach(proposition -> propositions.add(new PropositionSelectionDto.Proposition(proposition.getId(),
                proposition.getGaps(),
                playerId.equals(proposition.getPlayerId()) || isSphinx)));
        return ResponseEntity.ok(new PropositionSelectionDto(roundId, propositions, round.getSelectionSubmissionEnd()));
    }
    //endregion
    
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
        var round = gameService.getCurrentRoundInGame(gameId, playerId);
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

    private static List<SelectionDto> createSelectionDtos(Game game, List<Proposition> propositions, HashMap<String, String> selections) {
        List<SelectionDto> selectionDtos = new ArrayList<>();
        propositions.forEach(proposition -> {
            List<String> selectors = new ArrayList<>();
            selections.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(proposition.getId()))
                    .forEach(entry -> selectors.add(game.getPlayerNameFromId(entry.getKey())));

            selectionDtos.add(
                    new SelectionDto(
                            Collections.singletonList(game.getPlayerNameFromId(proposition.getPlayerId())),
                            proposition.getGaps(),
                            selectors
                    ));
        });
        return selectionDtos;
    }
}
