package com.srikanth.agenticurlshortner;

import com.srikanth.agenticurlshortner.config.AgenticExecutionProperties;
import com.srikanth.agenticurlshortner.config.ModelProviderProperties;
import com.srikanth.agenticurlshortner.config.PatchPolicyProperties;
import com.srikanth.agenticurlshortner.config.RepositoryToolProperties;
import com.srikanth.agenticurlshortner.config.ValidationProperties;
import com.srikanth.agenticurlshortner.config.GovernanceProperties;
import com.srikanth.agenticurlshortner.coordination.CoordinationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({AgenticExecutionProperties.class, RepositoryToolProperties.class,
        ModelProviderProperties.class, PatchPolicyProperties.class, ValidationProperties.class,
        GovernanceProperties.class, CoordinationProperties.class})
@EnableAsync
@EnableScheduling
public class AgenticUrlShortnerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgenticUrlShortnerApplication.class, args);
    }
}
