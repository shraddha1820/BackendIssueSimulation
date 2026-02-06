package com.example.primeGenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PrimeGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrimeGeneratorApplication.class, args);
	}

}
