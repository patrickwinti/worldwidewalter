package ch.zhaw.www;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
/**

 This class serves as a configuration file for the servlet initializer in Spring Boot.

 It extends the {@link SpringBootServletInitializer} class, which is used to bootstrap the

 application for use in a servlet container. It overrides the {@link #configure} method to

 specify the main application class to be used.
 */
public class ServletInitializer extends SpringBootServletInitializer {
    /**

     Configures the application builder by specifying the main application class.
     @param application The Spring application builder.
     @return The configured Spring application builder.
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DomainApplication.class);
    }

}
