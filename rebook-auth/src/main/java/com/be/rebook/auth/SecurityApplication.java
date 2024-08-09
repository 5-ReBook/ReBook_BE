package com.be.rebook.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.be.rebook.common", "com.be.rebook.auth"})
public class SecurityApplication {
    public static void main(String[] args) {
        // Spring Boot 애플리케이션 실행
        SpringApplication.run(SecurityApplication.class, args);
    }
}
