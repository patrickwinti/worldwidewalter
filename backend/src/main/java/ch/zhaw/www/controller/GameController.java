package ch.zhaw.www.controller;


import ch.zhaw.www.dto.GameDto;
import ch.zhaw.www.dto.PlayerDto;
import ch.zhaw.www.dto.PlayerJoinDto;
import ch.zhaw.www.model.Game;
import ch.zhaw.www.model.Player;
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

/**
 * Controller for "games" resource.
 **/
@RequestMapping("/api")
@RestController
@Validated
public class GameController {
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
        return ResponseEntity.ok(new GameDto(newGame.getId(), String.format("/api/games/%s", newGame.getId())));
    }


    @Operation(summary = "Register user and join them to an existing game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player has been added to game"),
            @ApiResponse(responseCode = "404", description = "Game has not been found"),
            @ApiResponse(responseCode = "409", description = "Game has reached max capacity"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/games/{gameId}/players", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PlayerDto> registerPlayer(@PathVariable String gameId, @Valid @RequestBody PlayerJoinDto playerDto) {
        Player player = gameService.registerUser(gameId, playerDto.getPlayerName());
        return ResponseEntity.ok(new PlayerDto(player.getId()));
    }
}
