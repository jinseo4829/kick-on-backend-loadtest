package kr.kickon.api.global.common.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class PerformanceAspect {

    @Around("execution(* kr.kickon.api.domain..*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();

        String methodName = joinPoint.getSignature().toShortString();
        stopWatch.start(methodName);

        Object result = joinPoint.proceed(); // 메서드 실행

        stopWatch.stop();
        System.out.println(methodName + " 실행 시간: " + stopWatch.getTotalTimeMillis() + "ms");

        return result;
    }
}