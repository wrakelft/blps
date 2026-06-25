package ru.itmo.blps1.dto.camunda;

import java.util.Map;

public record CamundaFormFieldResponse(
        String id,
        String label,
        String type,
        Object defaultValue,
        Map<String, String> values
) {
}