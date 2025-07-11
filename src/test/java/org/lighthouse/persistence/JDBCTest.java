package org.lighthouse.persistence;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class JDBCTest {

    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    @BeforeAll
    public static void setup() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("src/test/resources/application-test.properties"));

            driver = props.getProperty("jdbc.driver");
            url = props.getProperty("jdbc.url");
            username = props.getProperty("jdbc.username");
            password = props.getProperty("jdbc.password");

            Class.forName(driver);
        } catch(Exception e) {
            e.printStackTrace();
            fail("설정 파일 로딩 실패");
        }
    }

    @Test
    @DisplayName("JDBC 드라이버 연결이 된다.")
    public void testConnection() {
        try(Connection con = DriverManager.getConnection(url, username, password)) {
            log.info("DB 연결 성공: {}", con);
        } catch(Exception e) {
            fail("DB 연결 실패: " + e.getMessage());
        }
    }
}
