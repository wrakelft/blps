package ru.itmo.blps1.mapper;

import org.springframework.stereotype.Component;
import ru.itmo.blps1.dto.moderation.ModerationRequestResponse;
import ru.itmo.blps1.entity.BoardModerationRequest;

@Component
public class BoardModerationRequestMapper {

    public ModerationRequestResponse toResponse(BoardModerationRequest request) {
        if (request == null) {
            return null;
        }

        return new ModerationRequestResponse(
                request.getId(),

                request.getBoard() != null ? request.getBoard().getId() : null,
                request.getBoard() != null ? request.getBoard().getName() : null,

                request.getRequestedBy() != null ? request.getRequestedBy().getId() : null,
                request.getRequestedBy() != null ? request.getRequestedBy().getUsername() : null,

                request.getModerator() != null ? request.getModerator().getId() : null,
                request.getModerator() != null ? request.getModerator().getUsername() : null,

                request.getStatus(),
                request.getExternalSyncStatus(),
                request.getExternalSystem(),
                request.getExternalTaskId(),
                request.getComment(),
                request.getCreatedAt(),
                request.getProcessedAt(),
                request.getUpdatedAt()
        );
    }
}
