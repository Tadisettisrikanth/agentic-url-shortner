package com.srikanth.agenticurlshortner.agent;

import com.srikanth.agenticurlshortner.agent.SpecialistAgentModels.SpecialistAgentInput;
import com.srikanth.agenticurlshortner.agent.SpecialistAgentModels.SpecialistAgentResult;

public interface SpecialistAgent {
    SpecialistAgentRole role();
    SpecialistAgentResult execute(SpecialistAgentInput input);
}
