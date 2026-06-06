package ru.itmo.blps1.dto.moderation;

import lombok.Builder;
import lombok.Getter;
import ru.itmo.blps1.entity.enums.BoardModerationStatus;

@Getter
@Builder
public class SubmitBoardModerationResponse {

    private Long boardId;

    private BoardModerationStatus moderationStatus;

    private String message;
}
