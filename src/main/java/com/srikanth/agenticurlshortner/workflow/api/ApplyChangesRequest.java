package com.srikanth.agenticurlshortner.workflow.api;

import jakarta.validation.constraints.NotBlank;

public record ApplyChangesRequest(@NotBlank String planHash) {}
