package ru.itmo.blps1.service.moderation;

import ru.itmo.blps1.dto.moderation.SubmitBoardModerationResponse;

public interface BoardModerationServiceInt {

    SubmitBoardModerationResponse submitBoardForModeration(Long boardId);
}
