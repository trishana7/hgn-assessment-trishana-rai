package com.hgn.sosalertservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SosalertserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SosalertserviceApplication.class, args);
	}

}
