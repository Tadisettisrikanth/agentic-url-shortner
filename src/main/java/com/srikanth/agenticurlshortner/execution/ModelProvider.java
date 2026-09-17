package com.srikanth.agenticurlshortner.execution;

import com.srikanth.agenticurlshortner.execution.model.ExecutionModels.ModelRequest;
import com.srikanth.agenticurlshortner.execution.model.ExecutionModels.ModelResponse;

public interface ModelProvider {
    ModelResponse generate(ModelRequest request);
}

