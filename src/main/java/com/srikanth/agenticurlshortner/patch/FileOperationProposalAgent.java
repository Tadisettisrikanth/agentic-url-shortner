package com.srikanth.agenticurlshortner.patch;

import com.srikanth.agenticurlshortner.patch.PatchModels.AgentPatchProposal;
import java.util.List;
import java.util.Optional;

public interface FileOperationProposalAgent {
    List<AgentPatchProposal> propose(ProposalContext context);

    default Optional<AgentPatchProposal> proposeRepair(RepairProposalContext context) {
        return Optional.empty();
    }

    record ProposalContext(String requirementId, String normalizedRequirement,
                           List<String> acceptanceCriterionIds, String requirementHash,
                           String repositoryAnalysisHash, String planHash) {
        public ProposalContext { acceptanceCriterionIds = List.copyOf(acceptanceCriterionIds); }
    }

    record RepairProposalContext(String requirementId, List<String> acceptanceCriterionIds,
                                 String boundedFailureEvidence, String relevantSources,
                                 String priorProposalJson, List<String> inputArtifactHashes) {
        public RepairProposalContext {
            acceptanceCriterionIds = List.copyOf(acceptanceCriterionIds);
            inputArtifactHashes = List.copyOf(inputArtifactHashes);
        }
    }
}
