package com.srikanth.agenticurlshortner.config;

import com.srikanth.agenticurlshortner.validation.FixedMavenCapabilityTool;
import com.srikanth.agenticurlshortner.validation.RepairCoordinator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfiguration {
    @Bean
    FixedMavenCapabilityTool fixedMavenCapabilityTool(ValidationProperties properties) {
        return new FixedMavenCapabilityTool(properties);
    }

}
