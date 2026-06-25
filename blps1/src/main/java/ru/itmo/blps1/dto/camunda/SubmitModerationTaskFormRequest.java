package ru.itmo.blps1.dto.camunda;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubmitModerationTaskFormRequest(
        @NotBlank(message = "Moderation decision is required")
        @Pattern(regexp = "APPROVED|REJECTED", message = "Moderation decision must be APPROVED or REJECTED")
        String moderationDecision,

        @Size(max = 2000, message = "Comment must be less than 2000 characters")
        String moderatorComment
) {
}