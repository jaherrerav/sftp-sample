package com.example.sftp_sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * Global OpenAPI 3 metadata for the SFTP Integration API.
 */
@Configuration
public class OpenApiConfig {

    /** Builds the top-level API descriptor with title, description and version. */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SFTP Integration API")
                .description("API para operaciones de transferencia de archivos SFTP")
                .version("1.0.0"));
    }
}
