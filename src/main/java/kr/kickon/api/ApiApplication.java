package kr.kickon.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

//@SpringBootApplication(scanBasePackages = "kr.kickon.api")
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class ApiApplication {
    public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(ApiApplication.class, args);
	}

}
