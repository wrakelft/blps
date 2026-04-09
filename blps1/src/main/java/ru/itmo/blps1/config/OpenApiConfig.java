package ru.itmo.blps1.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blpsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("BLPS Pinterest API")
                        .description("REST API for boards, pins, file upload and board-pin workflows")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Gleb")
                                .email("gleb@alfn.ru"))
                        .license(new License()
                                .name("For academic use")));
    }
}
