package ch.zhaw.www;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class HelloWorldController {
    @GetMapping(value = "/world", produces = "application/json")
    public ResponseEntity<testDTO> world() {
        return ResponseEntity.ok(new testDTO("hello world!", 1));
    }

    @GetMapping(value = "/moon", produces = "application/json")
    public ResponseEntity<testDTO> moon() {
        return ResponseEntity.ok(new testDTO("hello moon!", 1));
    }
}