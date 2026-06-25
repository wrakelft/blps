package ru.itmo.blps1.dto.camunda;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SubmitStartFormRequest(
        @NotNull(message = "Variables are required")
        Map<String, Object> variables
) {
}