package kr.kickon.api.global.config;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.classic.spi.ILoggingEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import java.io.File;

@Slf4j
@Configuration
public class LogbackConfig {

    private static final String LOG_DIR = "/var/log/kickon-server";

    @PostConstruct
    public void setupLogger() {
        // 디렉토리 없으면 생성
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                log.error("Failed to create log directory: {}", LOG_DIR);
            } else {
                log.info("Log directory created: {}", LOG_DIR);
            }
        }

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // 로그 포맷 정의
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n");
        encoder.start();

        // Console Appender 추가
        ConsoleAppender<ILoggingEvent> consoleAppender = buildConsoleAppender(context, encoder);

        // API 로그 Appender
        RollingFileAppender<ILoggingEvent> apiAppender = buildRollingAppender(context,
                "API", LOG_DIR + "/api.log", LOG_DIR + "/api-%d{yyyy-MM-dd}.log", encoder);
        Logger apiLogger = context.getLogger("API_LOGGER");
        apiLogger.setAdditive(false);
        apiLogger.setLevel(Level.INFO);
        apiLogger.addAppender(apiAppender);
        apiLogger.addAppender(consoleAppender);

        // Migration 로그 Appender
        RollingFileAppender<ILoggingEvent> migrationAppender = buildRollingAppender(context,
                "MIGRATION", LOG_DIR + "/migration.log", LOG_DIR + "/migration-%d{yyyy-MM-dd}.log", encoder);
        Logger migrationLogger = context.getLogger("MIGRATION_LOGGER");
        migrationLogger.setAdditive(false);
        migrationLogger.setLevel(Level.INFO);
        migrationLogger.addAppender(migrationAppender);
        migrationLogger.addAppender(consoleAppender);

        // ERROR 로그 Appender (ERROR만 기록)
        RollingFileAppender<ILoggingEvent> errorAppender = buildRollingAppender(context,
                "ERROR", LOG_DIR + "/error.log", LOG_DIR + "/error-%d{yyyy-MM-dd}.log", encoder);
        Logger errorLogger = context.getLogger("ERROR_LOGGER");
        errorLogger.setAdditive(false);  // rootLogger에 영향 없도록 설정
        errorLogger.setLevel(Level.ERROR);  // ERROR만 기록
        errorLogger.addAppender(errorAppender);
        errorLogger.addAppender(consoleAppender);
    }

    private ConsoleAppender<ILoggingEvent> buildConsoleAppender(LoggerContext context, PatternLayoutEncoder encoder) {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("CONSOLE");
        appender.setEncoder(encoder);
        appender.start();
        return appender;
    }

    private RollingFileAppender<ILoggingEvent> buildRollingAppender(LoggerContext context, String name,
                                                                    String logFile, String fileNamePattern,
                                                                    PatternLayoutEncoder encoder) {
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName(name + "_APPENDER");
        appender.setFile(logFile);

        TimeBasedRollingPolicy<ILoggingEvent> policy = new TimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern(fileNamePattern);
        policy.setMaxHistory(30);
        policy.start();

        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);

        if (name.equals("ERROR")) {
            ThresholdFilter errorFilter = new ThresholdFilter();
            errorFilter.setLevel("ERROR");
            errorFilter.start();
            appender.addFilter(errorFilter);
        }

        appender.start();
        return appender;
    }
}