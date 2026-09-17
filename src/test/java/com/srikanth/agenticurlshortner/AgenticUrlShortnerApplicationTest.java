package com.srikanth.agenticurlshortner;

import static org.assertj.core.api.Assertions.assertThat;

import com.srikanth.agenticurlshortner.config.AgenticExecutionProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AgenticUrlShortnerApplicationTest {
    @Autowired AgenticExecutionProperties executionProperties;

    @Test
    void contextLoadsWithFlywaySchema() {
        assertThat(executionProperties.deterministic()).isTrue();
        assertThat(executionProperties.maxAttempts()).isEqualTo(3);
        assertThat(executionProperties.workspaceRoot()).hasToString("agent-workspaces");
        assertThat(executionProperties.clarificationToken()).isEqualTo("local-operator-token");
    }
}
