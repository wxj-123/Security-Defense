package com.example.securitydefense.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Druid配置类
 */
@Configuration
public class DruidConfig {

    /**
     * 配置Druid数据源
     */
    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean
    public DataSource druidDataSource() {
        return new DruidDataSource();
    }

    /**
     * 配置Druid监控Servlet
     */
    @Bean
    public ServletRegistrationBean<StatViewServlet> statViewServlet() {
        ServletRegistrationBean<StatViewServlet> bean = new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        
        Map<String, String> initParams = new HashMap<>();
        // 配置登录用户名和密码
        initParams.put("loginUsername", "admin");
        initParams.put("loginPassword", "admin");
        // 允许访问的IP地址，不配置默认都可以访问
        initParams.put("allow", "");
        // 拒绝访问的IP地址
        initParams.put("deny", "");
        // 是否可以重置数据
        initParams.put("resetEnable", "true");
        
        bean.setInitParameters(initParams);
        return bean;
    }

    /**
     * 配置Druid监控Filter
     */
    @Bean
    public FilterRegistrationBean<WebStatFilter> webStatFilter() {
        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new WebStatFilter());
        
        Map<String, String> initParams = new HashMap<>();
        // 排除静态资源
        initParams.put("exclusions", "*.js,*.css,/druid/*");
        
        bean.setInitParameters(initParams);
        bean.setUrlPatterns(java.util.Arrays.asList("/*"));
        return bean;
    }
}