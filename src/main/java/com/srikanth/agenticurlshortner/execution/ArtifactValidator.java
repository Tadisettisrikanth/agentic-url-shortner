package com.srikanth.agenticurlshortner.execution;

import com.srikanth.agenticurlshortner.artifact.ArtifactModels.EngineeringArtifact;
import com.srikanth.agenticurlshortner.artifact.ArtifactModels.ValidationResult;
import com.srikanth.agenticurlshortner.execution.model.ExecutionModels.ValidationContext;

public interface ArtifactValidator {
    ValidationResult validate(EngineeringArtifact artifact, ValidationContext context);
}

