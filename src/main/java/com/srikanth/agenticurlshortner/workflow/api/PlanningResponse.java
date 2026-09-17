package com.srikanth.agenticurlshortner.workflow.api;

import com.srikanth.agenticurlshortner.planning.domain.PlanModels.EngineeringTaskPlan;
import com.srikanth.agenticurlshortner.repository.domain.RepositoryModels.RepositoryMap;
import com.srikanth.agenticurlshortner.workflow.domain.WorkflowStatus;
import java.util.UUID;

public record PlanningResponse(UUID workflowId, UUID revisionId, WorkflowStatus status,
                               String workspaceLocation, String baselineManifestHash,
                               String repositoryAnalysisHash, RepositoryMap repositoryAnalysis,
                               String planHash, EngineeringTaskPlan plan) {
}
