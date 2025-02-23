package kr.kickon.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//@SpringBootApplication(scanBasePackages = "kr.kickon.api")
@SpringBootApplication
@EnableJpaAuditing
public class ApiApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(ApiApplication.class, args);
	}

}
