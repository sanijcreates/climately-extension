package com.backend.hh24;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class Hh24backendApplication {

	public static void main(String[] args) {
		SpringApplication.run(Hh24backendApplication.class, args);
	}

}
