package kr.kickon.api;

import kr.kickon.api.global.util.slack.SlackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

//@SpringBootApplication(scanBasePackages = "kr.kickon.api")
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableJpaRepositories(basePackages = "kr.kickon.api")
@Slf4j
public class ApiApplication {
    
    @Value("${spring.config.activate.on-profile:}")
    private String activeProfile;
    
    @Value("${KAKAO_REDIRECT_URI:}")
    private String kakaoRedirectUri;
    
    public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(ApiApplication.class, args);
		// ✅ SlackService 빈 가져오기
		SlackService slackService = ctx.getBean(SlackService.class);
		slackService.sendLogMessage("배포가 완료됐습니다!");
	}
	
	@PostConstruct
	public void logEnvironmentInfo() {
	    log.info("🚀 애플리케이션 시작 - 환경 정보:");
	    log.info("   📋 활성 프로필: {}", activeProfile);
	    log.info("   🔗 KAKAO_REDIRECT_URI: {}", kakaoRedirectUri);
	    
	    // 환경변수 직접 확인
	    String envKakaoRedirect = System.getenv("KAKAO_REDIRECT_URI");
	    log.info("   🌍 System.getenv('KAKAO_REDIRECT_URI'): {}", envKakaoRedirect);
	}

}
