package com.cricket.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CricketManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(CricketManagementSystemApplication.class, args);
	}

}
