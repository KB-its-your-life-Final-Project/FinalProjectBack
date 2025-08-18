package com.lighthouse.config;

import com.lighthouse.security.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@ActiveProfiles("local") // 필요하다면 "test" 로 바꾸고 application-test.properties 사용
class RootConfigTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TaskScheduler taskScheduler;

    @Test
    @DisplayName("DataSource 연결이 정상 동작한다.")
    void testDataSource() throws Exception {
        try (Connection con = dataSource.getConnection()) {
            assertNotNull(con, "DB 연결이 null이면 안 됩니다.");
            log.info("DataSource 준비 완료: {}", con);
        }
    }

    @Test
    @DisplayName("SqlSessionFactory가 정상 동작한다.")
    void testSqlSessionFactory() throws Exception {
        try (SqlSession session = sqlSessionFactory.openSession();
             Connection con = session.getConnection()) {
            assertNotNull(session, "SqlSession이 null이면 안 됩니다.");
            assertNotNull(con, "DB Connection이 null이면 안 됩니다.");
            log.info("SqlSession: {}", session);
            log.info("DB Connection: {}", con);
        }
    }

    @Test
    @DisplayName("JavaMailSender Bean 로드 확인")
    void testMailSenderBean() {
        assertNotNull(mailSender, "MailSender Bean이 null이면 안 됩니다.");
        log.info("JavaMailSender: {}", mailSender);
    }

    @Test
    @DisplayName("TaskScheduler Bean 로드 확인")
    void testTaskSchedulerBean() {
        assertNotNull(taskScheduler, "TaskScheduler Bean이 null이면 안 됩니다.");
        log.info("TaskScheduler: {}", taskScheduler);
    }
}
