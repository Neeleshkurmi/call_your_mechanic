package com.nilesh.cym;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class CymApplication {

	public static void main(String[] args) {
		SpringApplication.run(CymApplication.class, args);
	}

}
