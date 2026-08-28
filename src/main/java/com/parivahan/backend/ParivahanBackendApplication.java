package com.parivahan.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParivahanBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParivahanBackendApplication.class, args);
	}

}
