package ru.itmo.blps1.service.moderation;

import ru.itmo.blps1.dto.moderation.SubmitBoardModerationResponse;
import ru.itmo.blps1.messaging.event.BoardModerationRequestEvent;

public interface BoardModerationServiceInt {

    SubmitBoardModerationResponse submitBoardForModeration(Long boardId);

    void processBoardModeration(BoardModerationRequestEvent event);
}
