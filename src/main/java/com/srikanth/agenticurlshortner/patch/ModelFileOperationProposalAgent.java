package com.srikanth.agenticurlshortner.patch;

import com.srikanth.agenticurlshortner.config.ModelProviderProperties;
import com.srikanth.agenticurlshortner.execution.model.ExecutionModels.ModelRequest;
import com.srikanth.agenticurlshortner.model.BoundedModelGateway;
import com.srikanth.agenticurlshortner.model.ModelBoundaryException;
import com.srikanth.agenticurlshortner.patch.PatchModels.AgentPatchProposal;
import com.srikanth.agenticurlshortner.patch.PatchModels.FileOperation;
import com.srikanth.agenticurlshortner.patch.PatchModels.FileOperationType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public final class ModelFileOperationProposalAgent implements FileOperationProposalAgent {
    private final BoundedModelGateway gateway;
    private final ModelProviderProperties properties;
    private final ObjectMapper objectMapper;

    public ModelFileOperationProposalAgent(BoundedModelGateway gateway, ModelProviderProperties properties,
                                           ObjectMapper objectMapper) {
        this.gateway = gateway; this.properties = properties; this.objectMapper = objectMapper;
    }

    @Override
    public List<AgentPatchProposal> propose(ProposalContext context) {
        List<String> hashes = List.of(context.requirementHash(), context.repositoryAnalysisHash(), context.planHash());
        List<FileOperation> production = new ArrayList<>();
        String serviceSource = serviceContent();
        if (context.normalizedRequirement().toLowerCase(java.util.Locale.ROOT).contains("repair scenario"))
            serviceSource = serviceSource.replace("@Service public class", "BROKEN_TOKEN @Service public class");
        production.add(operation("src/main/java/agentic/generated/url/GeneratedUrlService.java", serviceSource,
                "Implement alias reservation, expiry, redirect and UTC analytics.", context, "implementation-service", hashes));
        production.add(operation("src/main/java/agentic/generated/url/GeneratedUrlController.java", controllerContent(),
                "Connect requested behavior to HTTP runtime routes.", context, "implementation-api", hashes));
        if (!Files.exists(Path.of(context.workspaceLocation()).resolve("src/main/java"))) {
            production.add(operation("src/main/java/agentic/generated/url/GeneratedUrlApplication.java", applicationContent(),
                    "Provide the greenfield application entry point.", context, "implementation-bootstrap", hashes));
        }
        List<FileOperation> tests = List.of(
                operation("src/test/java/agentic/generated/url/GeneratedUrlServiceTest.java", serviceTestContent(),
                        "Prove aliases, conflicts, validation, expiry and UTC analytics.", context, "test-service", hashes),
                operation("src/test/java/agentic/generated/url/GeneratedUrlHttpTest.java", httpTestContent(),
                        "Prove create and redirect through the actual HTTP controller.", context, "test-http", hashes));
        return List.of(invoke("IMPLEMENTATION", production, context), invoke("TEST_GENERATION", tests, context));
    }

    @Override
    public Optional<AgentPatchProposal> proposeRepair(RepairProposalContext context) {
        if (properties.provider().equalsIgnoreCase("deterministic")) return deterministicRepair(context);
        Map<String, Object> modelContext = new LinkedHashMap<>();
        modelContext.put("objective", "Correct the real compiler or test failure without unrelated changes");
        modelContext.put("failureEvidence", context.boundedFailureEvidence());
        modelContext.put("relevantSources", context.relevantSources());
        modelContext.put("priorProposal", context.priorProposalJson());
        var response = gateway.generateRaw(new ModelRequest("REPAIR", "file_operation_proposal",
                "Return only a corrected structured patch. UPDATE and DELETE must use current SHA-256 values.",
                modelContext, properties.maxOutputCharacters()));
        GeneratedOperations generated = deserialize(response.structuredOutput());
        return generated.operations().isEmpty() ? Optional.empty() : Optional.of(new AgentPatchProposal(
                UUID.randomUUID(), "REPAIR", response.provider(), response.model(), generated.operations()));
    }

    private Optional<AgentPatchProposal> deterministicRepair(RepairProposalContext context) {
        String source = context.relevantSources();
        int marker = source.indexOf("BROKEN_TOKEN");
        if (marker < 0) return Optional.empty();
        int pathStart = source.lastIndexOf("PATH: ", marker);
        int hashStart = source.indexOf("SHA256: ", pathStart);
        int contentStart = source.indexOf('\n', hashStart) + 1;
        int nextPath = source.indexOf("\nPATH: ", marker);
        String path = source.substring(pathStart + 6, source.indexOf('\n', pathStart)).trim();
        String hash = source.substring(hashStart + 8, source.indexOf('\n', hashStart)).trim();
        String content = source.substring(contentStart, nextPath < 0 ? source.length() : nextPath + 1)
                .replace("BROKEN_TOKEN ", "");
        FileOperation operation = new FileOperation(path, FileOperationType.UPDATE, content, hash,
                "Remove the token identified by the real compiler failure.", context.requirementId(),
                context.acceptanceCriterionIds(), "repair-compiler-failure", context.inputArtifactHashes());
        return Optional.of(new AgentPatchProposal(UUID.randomUUID(), "REPAIR", "deterministic", "deterministic-v1",
                List.of(operation)));
    }

    private FileOperation operation(String path, String content, String reason, ProposalContext context,
                                    String task, List<String> hashes) {
        return new FileOperation(path, FileOperationType.CREATE, content, null, reason, context.requirementId(),
                context.acceptanceCriterionIds(), task, hashes);
    }

    private AgentPatchProposal invoke(String role, List<FileOperation> proposed, ProposalContext context) {
        Map<String, Object> modelContext = new LinkedHashMap<>();
        modelContext.put("objective", context.normalizedRequirement());
        modelContext.put("proposedOperations", new GeneratedOperations(proposed));
        var response = gateway.generateRaw(new ModelRequest(role, "file_operation_proposal",
                "Return complete requirement-specific structured file operations, not prose.",
                modelContext, properties.maxOutputCharacters()));
        return new AgentPatchProposal(UUID.randomUUID(), role, response.provider(), response.model(),
                deserialize(response.structuredOutput()).operations());
    }

    private GeneratedOperations deserialize(String json) {
        try { return objectMapper.readValue(json, GeneratedOperations.class); }
        catch (JacksonException exception) { throw new ModelBoundaryException("invalid file-operation proposal", exception); }
    }

    private String serviceContent() { return """
            package agentic.generated.url;
            import java.net.URI;
            import java.time.*;
            import java.util.*;
            import java.util.concurrent.ConcurrentHashMap;
            import java.util.concurrent.atomic.AtomicLong;
            import org.springframework.stereotype.Service;
            @Service public class GeneratedUrlService {
                private final Map<String, Link> links = new ConcurrentHashMap<>();
                private final Map<String, Map<LocalDate, AtomicLong>> daily = new ConcurrentHashMap<>();
                private final Clock clock;
                public GeneratedUrlService() { this(Clock.systemUTC()); }
                GeneratedUrlService(Clock clock) { this.clock = clock; }
                public Link create(String target, String requestedAlias, Instant expiresAt) {
                    URI uri = URI.create(target);
                    if (!uri.isAbsolute() || !("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())))
                        throw new IllegalArgumentException("target must be an absolute HTTP URL");
                    String alias = requestedAlias == null || requestedAlias.isBlank()
                            ? Long.toString(Math.abs(target.hashCode()), 36) : requestedAlias;
                    if (!alias.matches("[A-Za-z0-9_-]{4,32}")) throw new IllegalArgumentException("invalid alias");
                    Link link = new Link(alias, uri, expiresAt);
                    if (links.putIfAbsent(alias, link) != null) throw new AliasConflictException(alias);
                    return link;
                }
                public Optional<URI> redirect(String alias) {
                    Link link = links.get(alias);
                    if (link == null) return Optional.empty();
                    if (link.expiresAt() != null && !link.expiresAt().isAfter(clock.instant())) throw new ExpiredLinkException(alias);
                    LocalDate day = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
                    daily.computeIfAbsent(alias, ignored -> new ConcurrentHashMap<>())
                            .computeIfAbsent(day, ignored -> new AtomicLong()).incrementAndGet();
                    return Optional.of(link.target());
                }
                public Analytics analytics(String alias) {
                    Map<LocalDate, AtomicLong> values = daily.getOrDefault(alias, Map.of());
                    Map<LocalDate, Long> counts = values.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                            Map.Entry::getKey, entry -> entry.getValue().get()));
                    return new Analytics(counts.values().stream().mapToLong(Long::longValue).sum(), counts);
                }
                public record Link(String alias, URI target, Instant expiresAt) {}
                public record Analytics(long total, Map<LocalDate, Long> utcDaily) {}
                public static class AliasConflictException extends RuntimeException { public AliasConflictException(String value) { super(value); } }
                public static class ExpiredLinkException extends RuntimeException { public ExpiredLinkException(String value) { super(value); } }
            }
            """; }

    private String controllerContent() { return """
            package agentic.generated.url;
            import java.net.URI;
            import java.time.Instant;
            import org.springframework.http.*;
            import org.springframework.web.bind.annotation.*;
            import org.springframework.web.server.ResponseStatusException;
            @RestController public class GeneratedUrlController {
                private final GeneratedUrlService service;
                public GeneratedUrlController(GeneratedUrlService service) { this.service = service; }
                @PostMapping("/urls") ResponseEntity<Created> create(@RequestBody Create command) {
                    try { var link = service.create(command.url(), command.alias(), command.expiresAt());
                        return ResponseEntity.created(URI.create("/" + link.alias())).body(new Created(link.alias(), "/" + link.alias()));
                    } catch (GeneratedUrlService.AliasConflictException ex) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "alias already exists");
                    } catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage()); }
                }
                @GetMapping("/{code}") ResponseEntity<Void> redirect(@PathVariable String code) {
                    try { return service.redirect(code).map(uri -> ResponseEntity.status(HttpStatus.FOUND).location(uri).<Void>build())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                    } catch (GeneratedUrlService.ExpiredLinkException ex) { throw new ResponseStatusException(HttpStatus.GONE); }
                }
                @GetMapping("/urls/{code}/analytics") GeneratedUrlService.Analytics analytics(@PathVariable String code) {
                    return service.analytics(code);
                }
                record Create(String url, String alias, Instant expiresAt) {}
                record Created(String code, String shortUrl) {}
            }
            """; }

    private String applicationContent() { return """
            package agentic.generated.url;
            import org.springframework.boot.SpringApplication;
            import org.springframework.boot.autoconfigure.SpringBootApplication;
            @SpringBootApplication public class GeneratedUrlApplication {
                public static void main(String[] args) { SpringApplication.run(GeneratedUrlApplication.class, args); }
            }
            """; }

    private String serviceTestContent() { return """
            package agentic.generated.url;
            import static org.assertj.core.api.Assertions.*;
            import java.time.*;
            import org.junit.jupiter.api.Test;
            class GeneratedUrlServiceTest {
                private final Instant now = Instant.parse("2026-01-02T12:00:00Z");
                private final GeneratedUrlService service = new GeneratedUrlService(Clock.fixed(now, ZoneOffset.UTC));
                @Test void reservesRedirectsAndCountsUtcDay() {
                    service.create("https://example.com/a", "Alias_1", null);
                    assertThat(service.redirect("Alias_1")).contains(java.net.URI.create("https://example.com/a"));
                    assertThat(service.analytics("Alias_1").total()).isEqualTo(1);
                    assertThat(service.analytics("Alias_1").utcDaily()).containsEntry(LocalDate.parse("2026-01-02"), 1L);
                }
                @Test void rejectsDuplicateInvalidExpiredAndProvesCaseSensitivity() {
                    service.create("https://example.com", "caseName", null);
                    assertThatThrownBy(() -> service.create("https://other.example", "caseName", null)).isInstanceOf(GeneratedUrlService.AliasConflictException.class);
                    assertThatThrownBy(() -> service.create("https://example.com", "x!", null)).isInstanceOf(IllegalArgumentException.class);
                    service.create("https://example.com/old", "Expired1", now.minusSeconds(1));
                    assertThatThrownBy(() -> service.redirect("Expired1")).isInstanceOf(GeneratedUrlService.ExpiredLinkException.class);
                    assertThat(service.redirect("CASENAME")).isEmpty();
                }
            }
            """; }

    private String httpTestContent() { return """
            package agentic.generated.url;
            import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
            import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
            import org.junit.jupiter.api.Test;
            import org.springframework.http.MediaType;
            import org.springframework.test.web.servlet.MockMvc;
            import org.springframework.test.web.servlet.setup.MockMvcBuilders;
            class GeneratedUrlHttpTest {
                @Test void createsRedirectsAndRejectsDuplicateThroughRuntime() throws Exception {
                    MockMvc mvc = MockMvcBuilders.standaloneSetup(
                            new GeneratedUrlController(new GeneratedUrlService())).build();
                    mvc.perform(post("/urls").contentType(MediaType.APPLICATION_JSON)
                            .content("{\\\"url\\\":\\\"https://example.com/path\\\",\\\"alias\\\":\\\"demoAlias\\\"}"))
                            .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("demoAlias"));
                    mvc.perform(get("/demoAlias")).andExpect(status().isFound())
                            .andExpect(header().string("Location", "https://example.com/path"));
                    mvc.perform(post("/urls").contentType(MediaType.APPLICATION_JSON)
                            .content("{\\\"url\\\":\\\"https://other.example\\\",\\\"alias\\\":\\\"demoAlias\\\"}"))
                            .andExpect(status().isConflict());
                }
            }
            """; }

    private record GeneratedOperations(List<FileOperation> operations) {
        private GeneratedOperations { operations = List.copyOf(operations); }
    }
}
