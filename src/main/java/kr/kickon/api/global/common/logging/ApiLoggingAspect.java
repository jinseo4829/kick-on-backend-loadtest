package kr.kickon.api.global.common.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * API 요청/응답 시간 및 에러를 로깅하는 AOP 설정 클래스
 */
@Slf4j
@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
public class ApiLoggingAspect {
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Pointcut 설정: @RestController가 붙은 클래스의 모든 메서드를 대상으로 지정
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {}

    /**
     * Around Advice: 실제 메서드 호출 전/후 시점을 감싸서 로깅 처리
     */

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object logScheduler(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Scheduled task started: {}", joinPoint.getSignature());
        Object result = joinPoint.proceed();
        log.info("Scheduled task ended: {}", joinPoint.getSignature());
        return result;
    }

    @Around("restControllerMethods()")
    public Object logApi(ProceedingJoinPoint joinPoint) throws Throwable {
        // HTTP 요청 정보 가져오기
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        String uri = request.getRequestURI();
        // /api, /oauth2, /migration 경로 외는 로깅 제외
        if (!(uri.startsWith("/api") || uri.startsWith("/oauth2") || uri.startsWith("/migration"))) {
            return joinPoint.proceed();
        }

        // 실행 시간 측정 시작
        long start = System.currentTimeMillis();

        // 사용자 PK 가져오기
        User user = jwtTokenProvider.getUserFromSecurityContext();
        String userId = (user != null) ? String.valueOf(user.getId()) : "null";
        // 파라미터 정보 가져오기
        Map<String, Object> pathVars = getPathVariables(joinPoint);
        Map<String, String[]> queryParams = request.getParameterMap();
        String requestBody = null;
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                requestBody = new String(buf, wrapper.getCharacterEncoding());
            }
        }

        try {
            // 실제 메서드 실행
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            // 로깅할 메시지 구성
            String logMessage = buildLogMessage(request, uri, joinPoint, duration, userId, pathVars, queryParams, requestBody);
            logMessage = logMessage + " ✅"; // 성공 메시지에 체크 아이콘 추가

            // 로깅 분기 처리
            logMessage(uri, logMessage);

            return result;

        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;

            String errorMessage = buildErrorMessage(request, uri, e, duration, userId, pathVars, queryParams, requestBody);
            logMessage(uri, errorMessage);
            throw e;
        }
    }

    // PathVariable 추출
    private Map<String, Object> getPathVariables(JoinPoint joinPoint) {
        Map<String, Object> pathVars = new HashMap<>();

        // Signature를 MethodSignature로 캐스팅
        Signature signature = joinPoint.getSignature();
        if (signature instanceof MethodSignature) {
            MethodSignature methodSignature = (MethodSignature) signature;
            String[] parameterNames = methodSignature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            Annotation[][] annotations = methodSignature.getMethod().getParameterAnnotations();

            // @PathVariable 추출
            for (int i = 0; i < annotations.length; i++) {
                for (Annotation annotation : annotations[i]) {
                    if (annotation instanceof PathVariable) {
                        String name = ((PathVariable) annotation).name();
                        // name 속성이 비어있으면 파라미터 이름을 사용
                        if (name.isEmpty()) {
                            name = parameterNames[i]; // 파라미터 이름을 사용
                        }
                        pathVars.put(name, args[i]);
                    }
                }
            }
        }

        return pathVars;
    }

    // API 로그 메시지 빌드
    private String buildLogMessage(HttpServletRequest request, String uri, ProceedingJoinPoint joinPoint, long duration, String userId, Map<String, Object> pathVars, Map<String, String[]> queryParams, String body) {
        StringBuilder logMessage = new StringBuilder();
        System.out.println(userId);
        logMessage.append(String.format("[%s] %s - %s (%dms)",
                request.getMethod(), uri, joinPoint.getSignature(), duration));

        if (userId != null && !userId.isBlank()) {
            logMessage.append(String.format("\n├─ UserID: %s", userId));
        }

        if (pathVars != null && !pathVars.isEmpty()) {
            logMessage.append(String.format("\n├─ PathVars: %s", pathVars));
        }

        if (queryParams != null && !queryParams.isEmpty()) {
            logMessage.append(String.format("\n├─ QueryParams: %s", queryParams));
        }

        if (body != null && !body.isEmpty()) {
            logMessage.append("\n└─ Body:\n").append(body);
        }

        return logMessage.toString();
    }

    // 에러 로그 메시지 빌드
    private String buildErrorMessage(HttpServletRequest request, String uri, Throwable e, long duration, String userId, Map<String, Object> pathVars, Map<String, String[]> queryParams, String body) {
        StringBuilder logMessage = new StringBuilder();

        logMessage.append(String.format(
                "[%s] %s - ERROR (%s) - %dms",
                request.getMethod(), uri, e.getMessage(), duration
        ));

        if (userId != null && !userId.isBlank()) {
            logMessage.append(String.format("\n├─ UserID: %s", userId));
        }

        if (pathVars != null && !pathVars.isEmpty()) {
            logMessage.append(String.format("\n├─ PathVars: %s", pathVars));
        }

        if (queryParams != null && !queryParams.isEmpty()) {
            logMessage.append("\n├─ QueryParams: ");
            queryParams.forEach((key, value) -> logMessage.append(key).append("=").append(String.join(",", value)).append(", "));
            logMessage.setLength(logMessage.length() - 2); // 마지막 쉼표 제거
        }

        if (body != null && !body.isEmpty()) {
            logMessage.append("\n└─ Body:\n").append(body);
        }

        return logMessage.toString();
    }

    // 경로별 로깅 메서드
    private void logMessage(String uri, String message) {
        if (uri.startsWith("/migration")) {
            logMigration(message);
        } else {
            logApi(message);
        }
    }

    // API 로그용 logger
    private void logApi(String message) {
        LoggerFactory.getLogger("API_LOGGER").info(message);
    }

    // Migration 로그용 logger
    private void logMigration(String message) {
        LoggerFactory.getLogger("MIGRATION_LOGGER").info(message);
    }

}