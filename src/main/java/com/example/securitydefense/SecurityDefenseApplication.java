package com.example.securitydefense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 安全态势感知系统主应用
 */
@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
public class SecurityDefenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityDefenseApplication.class, args);
    }

}
