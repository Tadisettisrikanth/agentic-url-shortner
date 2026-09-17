package com.srikanth.agenticurlshortner.config;

import com.srikanth.agenticurlshortner.agent.ModelBackedSpecialistAgent;
import com.srikanth.agenticurlshortner.agent.SpecialistAgent;
import com.srikanth.agenticurlshortner.agent.SpecialistAgentRole;
import com.srikanth.agenticurlshortner.model.BoundedModelGateway;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class SpecialistAgentConfiguration {
    @Bean
    List<SpecialistAgent> specialistAgents(BoundedModelGateway gateway, ObjectMapper objectMapper,
                                            ModelProviderProperties properties) {
        return Arrays.stream(SpecialistAgentRole.values())
                .map(role -> (SpecialistAgent) new ModelBackedSpecialistAgent(
                        role, gateway, objectMapper, properties.maxOutputCharacters()))
                .toList();
    }
}
