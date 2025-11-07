package com.mi.im.group;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.mi.im.group.mapper")
public class ImGroupServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImGroupServiceApplication.class, args);
    }

}