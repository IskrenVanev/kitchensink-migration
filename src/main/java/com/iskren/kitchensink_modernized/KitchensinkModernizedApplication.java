package com.iskren.kitchensink_modernized;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com.iskren")
@EnableMongoRepositories(basePackages = "com.iskren.repository")
public class KitchensinkModernizedApplication {

	public static void main(String[] args) {
		SpringApplication.run(KitchensinkModernizedApplication.class, args);
	}

}
