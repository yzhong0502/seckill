package com.demo.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAutoConfiguration
public class SeckillApplication {

	public static void main(String[] args) {
		System.out.println("Hello World!");
		SpringApplication.run(SeckillApplication.class, args);
	}

}
