package com.bazarak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BazarakBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BazarakBackendApplication.class, args);
		System.out.println("========================================");
		System.out.println("  🪙  Bazarak Backend is running!  🪙  ");
		System.out.println("========================================");
	}
}