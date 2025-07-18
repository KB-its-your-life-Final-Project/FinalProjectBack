package com.lighthouse.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * ApplicationContext 초기화 시점에 .env 파일을 로드하여
 * 환경변수를 시스템 프로퍼티에 설정하는 역할
 * 스프링 빈이 생성되기 전에 환경변수를 주입
 */
public class EnvLoader implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .load();
        // 1. SPRING_PROFILES_ACTIVE 우선 처리
        String springProfile = dotenv.get("SPRING_PROFILES_ACTIVE");
        if (springProfile != null && System.getProperty("spring.profiles.active") == null) {
            System.setProperty("spring.profiles.active", springProfile);
        }
        // 2. 나머지 환경 변수 처리
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }
}
