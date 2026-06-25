package ru.itmo.blps1.dto.camunda;

public record SubmitStartFormResponse(
        String processKey,
        String processInstanceId,
        String businessKey,
        String message
) {
}