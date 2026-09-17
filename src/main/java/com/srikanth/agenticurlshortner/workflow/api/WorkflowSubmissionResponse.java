package com.srikanth.agenticurlshortner.workflow.api;

import com.srikanth.agenticurlshortner.workflow.domain.WorkflowStatus;
import java.util.UUID;

public record WorkflowSubmissionResponse(UUID workflowId, UUID revisionId, int revision,
                                         WorkflowStatus status, String requirementHash) {
}

