package com.security.basic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BasicSecurityDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BasicSecurityDemoApplication.class, args);
	}

	@Bean
	public WelcomeController welcomeController() {
		return new WelcomeController();
	}

}
