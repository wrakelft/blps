package ru.itmo.blps1.dto.moderation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectModerationRequest(
        @NotBlank(message = "Comment is required")
        @Size(max = 2000, message = "Comment must be less then 2000 characters")
        String comment
) {
}