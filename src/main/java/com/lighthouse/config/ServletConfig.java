package com.lighthouse.config;

import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@EnableWebMvc
@ComponentScan(
        basePackages = "com.lighthouse",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = { Repository.class, Service.class})
        }
)
public class ServletConfig implements WebMvcConfigurer {
    @Value("${FILE_UPLOAD_DIR}")
    private String relativeUploadDir;

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // JSON 컨버터를 최우선으로 설정
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));
        converters.add(0, jsonConverter);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        // 클라이언트 요청 URL /resources/파일.png 를
        // 톰캣 내부의 webapp/resources/파일.png 경로와 매핑하여 제공
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");

        // Swagger UI 리소스를 위한 핸들러 설정
        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        // Swagger WebJar 리소스 설정
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        // Swagger 리소스 설정
        registry.addResourceHandler("/swagger-resources/**")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/v2/api-docs")
                .addResourceLocations("classpath:/META-INF/resources/");

        String absolutePath = Paths.get(System.getProperty("user.home"), relativeUploadDir).toUri().toString();
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(absolutePath);

    }
}
