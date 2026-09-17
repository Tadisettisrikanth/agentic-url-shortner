package com.srikanth.agenticurlshortner.workflow.api;

import com.srikanth.agenticurlshortner.workflow.domain.WorkflowStatus;
import java.util.UUID;

public record ClarificationResponse(UUID workflowId, UUID revisionId, UUID parentRevisionId,
                                    int revision, WorkflowStatus status) {
}

