package com.srikanth.agenticurlshortner;

import com.srikanth.agenticurlshortner.config.AgenticExecutionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties(AgenticExecutionProperties.class)
@EnableAsync
public class AgenticUrlShortnerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgenticUrlShortnerApplication.class, args);
    }
}
