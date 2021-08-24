package com.demo.seckill;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication(scanBasePackages = {"com.demo.seckill"})
@MapperScan("com.demo.seckill.repository")
public class SeckillApplication {


	public static void main(String[] args) {
		SpringApplication.run(SeckillApplication.class, args);
	}

}
