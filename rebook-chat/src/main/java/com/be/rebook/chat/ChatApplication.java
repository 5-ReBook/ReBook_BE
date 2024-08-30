package com.be.rebook.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaAuditing
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = { "com.be.rebook.common", "com.be.rebook.chat" })
@EnableAsync
public class ChatApplication {
    public static void main(String[] args) {
        // Spring Boot 애플리케이션 실행
        SpringApplication.run(ChatApplication.class, args);
    }
}
