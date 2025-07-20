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
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "com.lighthouse")
@ComponentScan(
        basePackages = "com.lighthouse",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.lighthouse\\.security\\..*")
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
        return new HikariDataSource(config);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactory = new SqlSessionFactoryBean();
        sqlSessionFactory.setConfigLocation(applicationContext.getResource("classpath:/mybatis-config.xml"));
        sqlSessionFactory.setDataSource(dataSource());

        // MyBatis 설정 파일 경로
        sqlSessionFactory.setConfigLocation(applicationContext.getResource("classpath:/mybatis-config.xml"));
        
        // Mapper XML 파일 위치 추가
        sqlSessionFactory.setMapperLocations(applicationContext.getResources("classpath:com/lighthouse/**/mapper/*.xml"));

        return sqlSessionFactory.getObject();
    }
    @Bean
    public DataSourceTransactionManager transactionManager(){
        return new DataSourceTransactionManager(dataSource());
    }


}