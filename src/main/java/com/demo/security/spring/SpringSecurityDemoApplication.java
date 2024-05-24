package com.demo.security.spring;

import com.demo.security.spring.controller.WelcomeController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringSecurityDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurityDemoApplication.class, args);
	}

	@Bean
	public WelcomeController welcomeController() {
		return new WelcomeController();
	}

}
