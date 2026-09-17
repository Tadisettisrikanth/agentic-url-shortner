package com.srikanth.agenticurlshortner.requirement.agent;

import com.srikanth.agenticurlshortner.requirement.domain.RequirementAnalysis;

public interface RequirementInterpreterAgent {
    RequirementAnalysis interpret(String requirement, String repositoryPath);
}

