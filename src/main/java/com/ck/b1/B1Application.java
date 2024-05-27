package com.ck.b1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class B1Application {
	
	private static Logger log = LoggerFactory.getLogger(B1Application.class);

	public static void main(String[] args) {
		SpringApplication.run(B1Application.class, args);
	}

}
