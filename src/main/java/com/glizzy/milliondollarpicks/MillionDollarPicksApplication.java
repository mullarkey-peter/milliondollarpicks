package com.glizzy.milliondollarpicks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MillionDollarPicksApplication {

    public static void main(String[] args) {
        SpringApplication.run(MillionDollarPicksApplication.class, args);
    }
}