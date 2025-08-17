package com.lighthouse.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;

/**
 * ApplicationContext 초기화 시점에 conf/.env 파일을 로드하여
 * 환경변수를 시스템 프로퍼티에 설정하는 역할
 * (운영 배포 시 Tomcat conf/.env만 사용)
 */
public class EnvLoader implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Dotenv dotenv;

        // Tomcat conf 디렉토리(/opt/tomcat9/conf/.env) 확인
        String catalinaBase = System.getProperty("catalina.base");
        File envFile = new File(catalinaBase + "/conf", ".env");

        if (!envFile.exists()) {
            throw new IllegalStateException(".env file not found in: " + envFile.getAbsolutePath());
        }

        dotenv = Dotenv.configure()
                .directory(envFile.getParent()) // conf 디렉토리
                .filename(".env")
                .load();

        System.out.println(".env loaded from conf: " + envFile.getAbsolutePath());

        // SPRING_PROFILES_ACTIVE 우선 처리
        String springProfile = dotenv.get("SPRING_PROFILES_ACTIVE");
        if (springProfile != null && System.getProperty("spring.profiles.active") == null) {
            System.setProperty("spring.profiles.active", springProfile);
        }

        // 나머지 환경 변수 처리
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }
}
