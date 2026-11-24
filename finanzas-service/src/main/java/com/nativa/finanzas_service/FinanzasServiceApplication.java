package com.nativa.finanzas_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FinanzasServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanzasServiceApplication.class, args);
	}

}

