package com.example.authproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("https://auth-project-production.up.railway.app"))
                .addServersItem(new Server().url("http://localhost:8080"))
                .info(new Info()
                        .title("Auth Project API")
                        .version("1.0")
                        .description("It is an API for Authentication Spring project")
                        .contact(new io.swagger.v3.oas.models.info.Contact().email("nastenka.brutskaya@mail.ru"))
                        .license(new io.swagger.v3.oas.models.info.License().name("Apache 2.0").url("http://springdoc.org")))
                        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                        .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("springshop-public")
                .pathsToMatch("/api/**")
                .build();
    }

}
