package com.linhanshopping.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({ "com.linhanshopping.backend.*", "com.linhanshopping.backend" })
@EnableJpaRepositories(basePackages = { "com.linhanshopping.backend.*" })
@EntityScan({ "com.linhanshopping.common.*" })
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
		// http://localhost:8082/LinhanShoppingBackend/users/page/1?sortField=firstName&sortDir=asc
	}

}
