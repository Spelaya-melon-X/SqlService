package com.codzilla.sqlservice.SqlService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = "com.codzilla.sqlservice.SqlService")
@EnableKafka
public class SqlServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(SqlServiceApplication.class, args);
	}
}