package com.coopcredit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.coopcredit.infrastructure.persistence.repository")
@EntityScan(basePackages = "com.coopcredit.infrastructure.persistence.entity")
public class CreditApplicationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreditApplicationServiceApplication.class, args);
	}

}
