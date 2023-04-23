package ch.zhaw.www;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.map.repository.config.EnableMapRepositories;

@SpringBootApplication
@EnableMapRepositories
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
