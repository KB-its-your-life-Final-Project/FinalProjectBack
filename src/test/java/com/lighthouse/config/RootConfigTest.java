package com.lighthouse.config;

import com.lighthouse.config.RootConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes= {RootConfig.class})
@ActiveProfiles("local")
@Slf4j
@TestPropertySource(locations = "classpath:application-local.properties")  // 직접 지정
class RootConfigTest {
    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("DataSource 연결이 된다.")
    public void dataSource() throws SQLException {
        try(Connection con = dataSource.getConnection()){
            assertNotNull(con, "DB 연결이 null이면 안 됩니다.");
            log.info("DataSource 준비 완료: {}", con);
        }
    }

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    @DisplayName("SqlSessionFactory가 정상 동작한다.")
    public void testSqlSessionFactory() {
        try (
                SqlSession session = sqlSessionFactory.openSession();
                Connection con = session.getConnection()
        ) {
            assertNotNull(con, "DB 연결이 null이면 안 됩니다.");
            log.info("SqlSession: {}", session);
            log.info("DB Connection: {}", con);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
