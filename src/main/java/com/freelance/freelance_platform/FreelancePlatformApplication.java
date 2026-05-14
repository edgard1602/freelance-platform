package com.freelance.freelance_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaAuditing
@EnableCaching
@EnableAsync
@ConfigurationPropertiesScan
@SpringBootApplication
public class FreelancePlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(FreelancePlatformApplication.class, args);
	}

}
