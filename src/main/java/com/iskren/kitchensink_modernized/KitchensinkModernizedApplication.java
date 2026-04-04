package com.iskren.kitchensink_modernized;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.iskren")
@EntityScan(basePackages = "com.iskren.model")
@EnableJpaRepositories(basePackages = "com.iskren.repository")
public class KitchensinkModernizedApplication {

	public static void main(String[] args) {
		SpringApplication.run(KitchensinkModernizedApplication.class, args);
	}

}
