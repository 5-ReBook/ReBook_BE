package com.be.rebook.members;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.be.rebook.common", "com.be.rebook.members"})
public class MembersApplication {
    public static void main(String[] args){
        SpringApplication.run(MembersApplication.class, args);
    }
}
