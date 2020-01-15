package com.chaosonic.swaggerport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class Application {

    @Value("${server.port}")
    private int serverPort;

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .host("localhost:" + serverPort);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
