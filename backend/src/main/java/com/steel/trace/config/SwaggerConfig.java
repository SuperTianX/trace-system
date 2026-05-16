package com.steel.trace.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("炉批卷追溯与报工入库对账系统 API")
                        .version("1.0.0")
                        .description("炉批卷追溯与报工入库对账系统后端接口文档")
                        .contact(new Contact().name("Steel Trace Team")));
    }
}
