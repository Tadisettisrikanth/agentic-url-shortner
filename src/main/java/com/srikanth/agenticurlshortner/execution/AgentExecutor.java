package com.srikanth.agenticurlshortner.execution;

import com.srikanth.agenticurlshortner.execution.model.ExecutionModels.AgentExecutionResult;
import com.srikanth.agenticurlshortner.execution.model.ExecutionModels.ExecutionContext;
import com.srikanth.agenticurlshortner.workflow.domain.AgentTask;

public interface AgentExecutor {
    AgentExecutionResult execute(AgentTask task, ExecutionContext context);
}

