package ch.zhaw.www.controller;


import ch.zhaw.www.dto.GameResponse;
import ch.zhaw.www.models.Game;
import ch.zhaw.www.services.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "500", description = "Unknown error")
    })
    @PostMapping(value = "/games", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GameResponse> createGame() {
        Game newGame = gameService.createGame();
        return ResponseEntity.ok(new GameResponse(newGame.getId(), String.format("/games/%s", newGame.getId())));
    }
}
