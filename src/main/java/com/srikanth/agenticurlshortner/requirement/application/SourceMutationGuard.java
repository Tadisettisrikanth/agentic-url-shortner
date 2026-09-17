package com.srikanth.agenticurlshortner.requirement.application;

import com.srikanth.agenticurlshortner.requirement.persistence.RequirementAnalysisRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SourceMutationGuard {
    private final RequirementAnalysisRepository analyses;

    public SourceMutationGuard(RequirementAnalysisRepository analyses) {
        this.analyses = analyses;
    }

    public void requireAllowed(UUID revisionId) {
        var analysis = analyses.findByRevisionId(revisionId)
                .orElseThrow(() -> new IllegalStateException("requirement analysis is not complete"));
        if (!analysis.isSourceMutationAllowed()) {
            throw new IllegalStateException("source mutation is blocked pending clarification");
        }
    }
}

