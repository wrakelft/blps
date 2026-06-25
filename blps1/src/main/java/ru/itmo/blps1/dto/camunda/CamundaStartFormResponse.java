package ru.itmo.blps1.dto.camunda;

import java.util.List;

public record CamundaStartFormResponse(
        String processKey,
        String processDefinitionId,
        String formKey,
        List<CamundaFormFieldResponse> fields
) {
}