package com.srikanth.agenticurlshortner.requirement.application;

import java.util.UUID;

public record RequirementSubmittedEvent(UUID workflowId, UUID revisionId, String requirement, String repositoryPath) {
}

