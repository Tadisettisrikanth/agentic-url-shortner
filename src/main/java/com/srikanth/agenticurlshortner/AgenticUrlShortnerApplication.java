package com.srikanth.agenticurlshortner;

import com.srikanth.agenticurlshortner.config.AgenticExecutionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AgenticExecutionProperties.class)
public class AgenticUrlShortnerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgenticUrlShortnerApplication.class, args);
    }
}
