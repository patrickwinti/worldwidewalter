package ch.zhaw.www;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DomainApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomainApplication.class, args);
    }

    @RestController
    public class HelloWorldController {
        @GetMapping("/")
        @CrossOrigin(origins = "http://localhost:4200")
        public ResponseEntity<String> index() {
            return ResponseEntity
                    .ok()
                    .body("hello world!");
        }

        @GetMapping("/moon")
        @CrossOrigin(origins = "http://localhost:4200")
        public String moon() {
            return "Hello Moon!";
        }
    }
}
