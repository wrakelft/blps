package ru.itmo.blps1.dto.moderation;

import ru.itmo.blps1.entity.enums.ExternalSyncStatus;
import ru.itmo.blps1.entity.enums.ModerationRequestStatus;

import java.time.OffsetDateTime;

public record ModerationRequestResponse(
    Long id,
    Long boardId,
    String boardName,
    Long requestedById,
    String requestedByUsername,
    Long moderatorId,
    String moderatorUsername,
    ModerationRequestStatus status,
    ExternalSyncStatus externalSyncStatus,
    String externalSystem,
    String externalTaskId,
    String comment,
    OffsetDateTime createdAt,
    OffsetDateTime processedAt,
    OffsetDateTime updatedAt
) {
}
