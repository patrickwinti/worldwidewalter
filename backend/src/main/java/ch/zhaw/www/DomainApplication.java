package ch.zhaw.www;

import ch.zhaw.www.bean.PostfixGenerator;
import ch.zhaw.www.bean.PostfixGeneratorImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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

    /**
     * Creates and returns a new instance of the {@link PostfixGeneratorImpl}.
     *
     * @return a new instance of the {@link PostfixGeneratorImpl}
     */
    @Bean
    public PostfixGenerator getPostFixGenerator() {
        return new PostfixGeneratorImpl();
    }
}
