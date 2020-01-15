package com.chaosonic.swaggerport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@org.springframework.boot.autoconfigure.SpringBootApplication
@EnableSwagger2
public class SpringBootApplication {

@Value("${server.port}")
private int serverPort;

@Bean
public Docket docket() {
	return new Docket(DocumentationType.SWAGGER_2)
			.host("localhost:" + serverPort);
}

	public static void main(String[] args) {
		SpringApplication.run(SpringBootApplication.class, args);
	}

}
