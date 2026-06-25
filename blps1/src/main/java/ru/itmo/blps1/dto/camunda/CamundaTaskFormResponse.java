package ru.itmo.blps1.dto.camunda;

import java.util.List;

public record CamundaTaskFormResponse(
        String taskId,
        String taskName,
        String formKey,
        List<CamundaFormFieldResponse> fields
) {
}