package ch.zhaw.www;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.map.repository.config.EnableMapRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableMapRepositories
@EnableScheduling

/**
 * The main class for the domain application.
 * Responsible for starting the Spring Boot application.
 */
public class DomainApplication {

    /**
     * The entry point of the application. Starts the Spring Boot application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DomainApplication.class, args);
    }

}
