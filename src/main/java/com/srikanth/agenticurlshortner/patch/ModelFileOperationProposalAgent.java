package com.srikanth.agenticurlshortner.patch;

import com.srikanth.agenticurlshortner.config.ModelProviderProperties;
import com.srikanth.agenticurlshortner.execution.model.ExecutionModels.ModelRequest;
import com.srikanth.agenticurlshortner.model.BoundedModelGateway;
import com.srikanth.agenticurlshortner.model.ModelBoundaryException;
import com.srikanth.agenticurlshortner.patch.PatchModels.AgentPatchProposal;
import com.srikanth.agenticurlshortner.patch.PatchModels.FileOperation;
import com.srikanth.agenticurlshortner.patch.PatchModels.FileOperationType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public final class ModelFileOperationProposalAgent implements FileOperationProposalAgent {
    private final BoundedModelGateway gateway;
    private final ModelProviderProperties properties;
    private final ObjectMapper objectMapper;

    public ModelFileOperationProposalAgent(BoundedModelGateway gateway, ModelProviderProperties properties,
                                           ObjectMapper objectMapper) {
        this.gateway = gateway;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<AgentPatchProposal> propose(ProposalContext context) {
        String suffix = context.requirementHash().substring(0, 12);
        String className = "GeneratedRequirement" + suffix;
        List<String> inputHashes = List.of(context.requirementHash(), context.repositoryAnalysisHash(), context.planHash());
        FileOperation production = new FileOperation(
                "src/main/java/agentic/generated/" + className + ".java", FileOperationType.CREATE,
                productionContent(className, context), null,
                "Represent the normalized requirement and acceptance criteria in the compiled production source set.",
                context.requirementId(), context.acceptanceCriterionIds(), "implementation-" + suffix, inputHashes);
        FileOperation test = new FileOperation(
                "src/test/java/agentic/generated/" + className + "Test.java", FileOperationType.CREATE,
                testContent(className, context), null,
                "Exercise the exact generated production contract from the discovered test source set.",
                context.requirementId(), context.acceptanceCriterionIds(), "test-generation-" + suffix, inputHashes);
        return List.of(invoke("IMPLEMENTATION", production, context),
                invoke("TEST_GENERATION", test, context));
    }

    @Override
    public Optional<AgentPatchProposal> proposeRepair(RepairProposalContext context) {
        if (properties.provider().equalsIgnoreCase("deterministic")) return Optional.empty();
        Map<String, Object> modelContext = new LinkedHashMap<>();
        modelContext.put("objective", "Correct the real compiler or test failure without unrelated changes");
        modelContext.put("failureEvidence", context.boundedFailureEvidence());
        modelContext.put("relevantSources", context.relevantSources());
        modelContext.put("priorProposal", context.priorProposalJson());
        var response = gateway.generateRaw(new ModelRequest("REPAIR", "file_operation_proposal",
                "Return only a corrected structured patch. Every UPDATE or DELETE must use the supplied current SHA-256. "
                        + "Preserve requirement, acceptance-criterion and input-hash lineage.",
                modelContext, properties.maxOutputCharacters()));
        GeneratedOperations generated = deserialize(response.structuredOutput());
        if (generated.operations().isEmpty()) return Optional.empty();
        return Optional.of(new AgentPatchProposal(UUID.randomUUID(), "REPAIR", response.provider(), response.model(),
                generated.operations()));
    }

    private AgentPatchProposal invoke(String role, FileOperation suggested, ProposalContext context) {
        Map<String, Object> modelContext = new LinkedHashMap<>();
        modelContext.put("objective", context.normalizedRequirement());
        modelContext.put("proposedOperations", new GeneratedOperations(List.of(suggested)));
        var response = gateway.generateRaw(new ModelRequest(role, "file_operation_proposal",
                "Return complete structured file operations for the current requirement. Do not return prose. "
                        + "Preserve requirement, criterion, task and input-hash lineage.",
                modelContext, properties.maxOutputCharacters()));
        GeneratedOperations generated = deserialize(response.structuredOutput());
        return new AgentPatchProposal(UUID.randomUUID(), role, response.provider(), response.model(),
                generated.operations());
    }

    private GeneratedOperations deserialize(String json) {
        try { return objectMapper.readValue(json, GeneratedOperations.class); }
        catch (JacksonException exception) {
            throw new ModelBoundaryException("model output does not match file-operation proposal schema", exception);
        }
    }

    private String productionContent(String className, ProposalContext context) {
        String criteria = context.acceptanceCriterionIds().stream()
                .map(value -> "\"" + escape(value) + "\"").collect(java.util.stream.Collectors.joining(", "));
        return """
                package agentic.generated;

                import java.util.List;

                public final class %s {
                    private %s() {}

                    public static String requirement() {
                        return "%s";
                    }

                    public static List<String> acceptanceCriteria() {
                        return List.of(%s);
                    }
                }
                """.formatted(className, className, escape(context.normalizedRequirement()), criteria);
    }

    private String testContent(String className, ProposalContext context) {
        return """
                package agentic.generated;

                import static org.assertj.core.api.Assertions.assertThat;

                import org.junit.jupiter.api.Test;

                class %sTest {
                    @Test
                    void exposesCurrentRequirementTraceability() {
                        assertThat(%s.requirement()).isEqualTo("%s");
                        assertThat(%s.acceptanceCriteria()).containsExactly(%s);
                    }
                }
                """.formatted(className, className, escape(context.normalizedRequirement()), className,
                context.acceptanceCriterionIds().stream().map(value -> "\"" + escape(value) + "\"")
                        .collect(java.util.stream.Collectors.joining(", ")));
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }

    private record GeneratedOperations(List<FileOperation> operations) {
        private GeneratedOperations { operations = List.copyOf(operations); }
    }
}
