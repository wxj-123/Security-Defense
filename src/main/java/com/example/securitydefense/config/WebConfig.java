package com.example.securitydefense.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 上传文件路径常量
     */
    @NonNull
    private static final String UPLOAD_LOCATION = "file:" + System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 配置头像上传目录的访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(UPLOAD_LOCATION);
    }

    /**
     * 配置内容协商
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }
}