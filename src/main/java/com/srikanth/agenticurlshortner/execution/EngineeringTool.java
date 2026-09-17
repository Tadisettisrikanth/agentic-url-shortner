package com.srikanth.agenticurlshortner.execution;

import com.srikanth.agenticurlshortner.execution.model.ExecutionModels.ToolRequest;
import com.srikanth.agenticurlshortner.execution.model.ExecutionModels.ToolResult;

public interface EngineeringTool {
    ToolResult execute(ToolRequest request);
}

