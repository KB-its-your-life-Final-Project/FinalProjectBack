package com.lighthouse.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = {
    "com.lighthouse.buildingRegister.mapper",
    "com.lighthouse.estate.mapper",
    "com.lighthouse.member.mapper",
    "com.lighthouse.safereport.mapper",
    "com.lighthouse.security.mapper",
    "com.lighthouse.toCoord.mapper",
    "com.lighthouse.transactions.mapper",
    "com.lighthouse.coord.mapper",
    "com.lighthouse.wishlist.mapper",
    "com.lighthouse.localinfo.mapper",
    "com.lighthouse.homeregister.mapper",
    "com.lighthouse.lawdCode.mapper",
    "com.lighthouse.localinfo.mapper"
})
@ComponentScan(
        basePackages = {"com.lighthouse"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class),
//                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.lighthouse\\.security\\..*")
        }
)
public class RootConfig {
    @Configuration
    @Profile("local")
    @PropertySource("classpath:application-local.properties")
    static class LocalProperties {
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }

    @Configuration
    @Profile("prod")
    @PropertySource("classpath:application-prod.properties")
    static class ProdProperties {
        @Bean
        public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }

    @Value("${jdbc.driver}") String driver;
    @Value("${jdbc.url}") String url;
    @Value("${jdbc.username}") String username;
    @Value("${jdbc.password}") String password;

    // HikariCP 설정 추가
    @Value("${JDBC_CONNECTION_TIMEOUT:10000}") int connectionTimeout;
    @Value("${JDBC_MAX_LIFETIME:600000}") int maxLifetime;
    @Value("${JDBC_IDLE_TIMEOUT:300000}") int idleTimeout;
    @Value("${JDBC_MAXIMUM_POOL_SIZE:5}") int maximumPoolSize;
    @Value("${JDBC_MINIMUM_IDLE:2}") int minimumIdle;

    private final ApplicationContext applicationContext;

    public RootConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        // HikariCP 설정 적용
        config.setConnectionTimeout(connectionTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setIdleTimeout(idleTimeout);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);

        return new HikariDataSource(config);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(applicationContext.getResource("classpath:/mybatis-config.xml"));
        sqlSessionFactory.setMapperLocations(applicationContext.getResources("classpath:/com/lighthouse/**/*.xml"));
        sqlSessionFactory.setDataSource(dataSource());

        // Mapper XML 파일 위치 추가
        sqlSessionFactory.setMapperLocations(applicationContext.getResources("classpath:com/lighthouse/**/mapper/*.xml"));

        return sqlSessionFactory.getObject();
    }
    @Bean
    public DataSourceTransactionManager transactionManager(){
        return new DataSourceTransactionManager(dataSource());
    }

    @Value("${MAIL_HOST}") String mailHost;
    @Value("${MAIL_PORT}") int mailPort;
    @Value("${MAIL_USERNAME}") String mailUsername;
    @Value("${MAIL_APP_PASSWORD}") String mailAppPassword;
    @Value("${MAIL_SSL_TRUST}") String mailSmtpSslTrust;

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailAppPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", mailSmtpSslTrust);

        return mailSender;
    }
}