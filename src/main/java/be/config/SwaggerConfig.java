package be.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.servers.*;
import org.springframework.context.annotation.*;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI swaggerOpenAPI() {
        return new OpenAPI()
                .addServersItem(server());
    }

    @Bean
    public Server server() {
        return new Server()
                .url("/")
                .description("Swagger will use same protocol (http/https) that current page uses.");
    }

}
