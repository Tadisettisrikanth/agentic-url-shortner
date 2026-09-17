package com.srikanth.agenticurlshortner.config;

import com.srikanth.agenticurlshortner.execution.ModelProvider;
import com.srikanth.agenticurlshortner.model.BoundedModelGateway;
import com.srikanth.agenticurlshortner.model.DeterministicModelProvider;
import com.srikanth.agenticurlshortner.model.JdkOpenAiTransport;
import com.srikanth.agenticurlshortner.model.OpenAiResponsesModelProvider;
import com.srikanth.agenticurlshortner.patch.FileOperationProposalAgent;
import com.srikanth.agenticurlshortner.patch.ModelFileOperationProposalAgent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import com.srikanth.agenticurlshortner.observability.PlatformMetrics;

@Configuration
public class ModelBoundaryConfiguration {
    @Bean
    @ConditionalOnProperty(name = "agentic.model.provider", havingValue = "deterministic", matchIfMissing = true)
    ModelProvider deterministicModelProvider(ObjectMapper objectMapper) {
        return new DeterministicModelProvider(objectMapper);
    }

    @Bean
    @ConditionalOnProperty(name = "agentic.model.provider", havingValue = "openai")
    ModelProvider openAiModelProvider(ModelProviderProperties properties, ObjectMapper objectMapper) {
        return new OpenAiResponsesModelProvider(properties, new JdkOpenAiTransport(), objectMapper);
    }

    @Bean
    BoundedModelGateway boundedModelGateway(ModelProvider provider, ModelProviderProperties properties,
                                            ObjectMapper objectMapper, PlatformMetrics metrics) {
        return new BoundedModelGateway(provider, properties, objectMapper, metrics);
    }

    @Bean
    FileOperationProposalAgent fileOperationProposalAgent(BoundedModelGateway gateway,
                                                          ModelProviderProperties properties,
                                                          ObjectMapper objectMapper) {
        return new ModelFileOperationProposalAgent(gateway, properties, objectMapper);
    }
}
