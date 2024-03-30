package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SpringBootApplication
public class DbToFileApplication {

	public static void main(String[] args) {
		SpringApplication.run(DbToFileApplication.class, args);
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
        context.start();
	}

}
