package ch.zhaw.www.controller;

import ch.zhaw.www.dto.PropositionSelectionDto;
import ch.zhaw.www.dto.PropositionSubmissionDto;
import ch.zhaw.www.model.Proposition;
import ch.zhaw.www.service.RoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for "rounds" resource.
 **/

@RequestMapping("/api")
@RestController
@Validated
@CrossOrigin({"http://localhost:4200", "http://worldwidewalter.ch", "https://160.85.253.247"})
public class RoundController {
    
    private final Logger logger = Logger.getLogger(RoundController.class.getSimpleName());
    private final RoundService roundService;
    
    public RoundController(final RoundService roundService) {
        this.roundService = roundService;
    }
    
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
            @ApiResponse(responseCode = "405", description = "Not all players have sent their proposition"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @GetMapping(value = "/rounds/{roundId}/propositions", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PropositionSelectionDto> getAllPropositionForRound(@PathVariable String roundId, @Valid @RequestHeader("X-PLAYER-ID") String playerId) {
        var round = roundService.getRoundReadyForSelections(roundId, playerId);
        logger.log(Level.INFO, "round selections returned {0}", round);
        var isSphinx = round.getSphinx() != null && round.getSphinx().getId().equals(playerId);
        List<PropositionSelectionDto.Proposition> propositions = round.getPropositions()
                .stream()
                .map(proposition -> new PropositionSelectionDto.Proposition(
                        proposition.getId(),
                        proposition.getGaps(),
                        proposition.getPlayerIds().size(),
                        isPropositionReadOnly(proposition, playerId, isSphinx)
                )).collect(Collectors.toList());
        Collections.shuffle(propositions);
        
        return ResponseEntity.ok(new PropositionSelectionDto(
                roundId,
                propositions,
                round.getSelectionSubmissionEnd()));
    }
    
    private static boolean isPropositionReadOnly(Proposition proposition, String playerId, boolean isSphinx) {
        return isSphinx || (proposition.getPlayerIds().contains(playerId) && proposition.getPlayerIds().size() == 1);
    }
}
