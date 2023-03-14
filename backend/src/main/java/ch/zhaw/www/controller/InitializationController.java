package ch.zhaw.www.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class InitializationController {
    /**
     * Create game endpoint
     *
     * @return the newly game session
     */
    @GetMapping(value = "/api/games", produces = "application/json")
    public ResponseEntity createGame() {
        System.out.println("called create game");
        return ResponseEntity.ok().build();
    }
}