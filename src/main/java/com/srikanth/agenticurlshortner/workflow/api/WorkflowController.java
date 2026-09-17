package com.srikanth.agenticurlshortner.workflow.api;

import com.srikanth.agenticurlshortner.requirement.application.ClarificationService;
import com.srikanth.agenticurlshortner.planning.RepositoryPlanningService;
import com.srikanth.agenticurlshortner.patch.PatchApplicationService;
import com.srikanth.agenticurlshortner.validation.BuildModels.ValidationOutcome;
import com.srikanth.agenticurlshortner.validation.WorkflowValidationService;
import com.srikanth.agenticurlshortner.governance.GovernanceService;
import com.srikanth.agenticurlshortner.governance.GovernanceRequests.ApprovalRequest;
import com.srikanth.agenticurlshortner.governance.GovernanceRequests.ApprovalResponse;
import com.srikanth.agenticurlshortner.governance.GovernanceRequests.OutcomeResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflows")
class WorkflowController {
    private final WorkflowSubmissionService service;
    private final WorkflowQueryService queryService;
    private final ClarificationService clarificationService;
    private final RepositoryPlanningService planningService;
    private final PatchApplicationService patchApplicationService;
    private final WorkflowValidationService validationService;
    private final GovernanceService governanceService;

    WorkflowController(WorkflowSubmissionService service, WorkflowQueryService queryService,
                       ClarificationService clarificationService, RepositoryPlanningService planningService,
                       PatchApplicationService patchApplicationService,
                       WorkflowValidationService validationService, GovernanceService governanceService) {
        this.service = service;
        this.queryService = queryService;
        this.clarificationService = clarificationService;
        this.planningService = planningService;
        this.patchApplicationService = patchApplicationService;
        this.validationService = validationService;
        this.governanceService = governanceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    WorkflowSubmissionResponse submit(@Valid @RequestBody CreateWorkflowRequest request) {
        return service.submit(request);
    }

    @GetMapping("/{workflowId}")
    WorkflowDetailsResponse get(@PathVariable UUID workflowId) {
        return queryService.get(workflowId);
    }

    @PostMapping("/{workflowId}/clarifications")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ClarificationResponse clarify(@PathVariable UUID workflowId,
                                  @RequestHeader("X-Operator-Token") String operatorToken,
                                  @RequestHeader("X-Operator-Id") String operatorId,
                                  @Valid @RequestBody ClarificationRequest request) {
        return clarificationService.clarify(workflowId, request, operatorToken, operatorId);
    }

    @PostMapping("/{workflowId}/plan")
    @ResponseStatus(HttpStatus.ACCEPTED)
    PlanningResponse plan(@PathVariable UUID workflowId) {
        return planningService.analyzeAndPlan(workflowId);
    }

    @PostMapping("/{workflowId}/changes/apply")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ApplyChangesResponse applyChanges(@PathVariable UUID workflowId,
                                      @Valid @RequestBody ApplyChangesRequest request) {
        return patchApplicationService.generateAndApply(workflowId, request);
    }

    @PostMapping("/{workflowId}/validate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    ValidationOutcome validate(@PathVariable UUID workflowId) {
        return validationService.validate(workflowId);
    }

    @PostMapping("/{workflowId}/approvals/change")
    ApprovalResponse approveChange(@PathVariable UUID workflowId, @Valid @RequestBody ApprovalRequest request,
                                   @RequestHeader("X-Change-Approver-Token") String token,
                                   @RequestHeader("X-Approver-Id") String approver) {
        return governanceService.approveChange(workflowId, request.evidenceHash(), token, approver);
    }

    @PostMapping("/{workflowId}/outcome")
    OutcomeResponse outcome(@PathVariable UUID workflowId) {
        return governanceService.generateOutcome(workflowId);
    }

    @PostMapping("/{workflowId}/approvals/release")
    ApprovalResponse approveRelease(@PathVariable UUID workflowId, @Valid @RequestBody ApprovalRequest request,
                                    @RequestHeader("X-Release-Approver-Token") String token,
                                    @RequestHeader("X-Approver-Id") String approver) {
        return governanceService.approveRelease(workflowId, request.evidenceHash(), token, approver);
    }

    @PostMapping("/{workflowId}/cancel")
    ApprovalResponse cancel(@PathVariable UUID workflowId,
                            @RequestHeader("X-Operator-Token") String token,
                            @RequestHeader("X-Operator-Id") String operator) {
        return governanceService.cancel(workflowId, token, operator);
    }
}
