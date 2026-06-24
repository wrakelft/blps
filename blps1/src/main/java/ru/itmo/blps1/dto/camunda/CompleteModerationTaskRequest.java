package ru.itmo.blps1.dto.camunda;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompleteModerationTaskRequest(
        @NotBlank(message = "Decision is required")
        @Pattern(regexp = "APPROVED|REJECTED", message = "Decision must be APPROVED or REJECTED")
        String decision,

        @Size(max = 2000, message = "Comment must be less than 2000 characters")
        String comment
) {
}