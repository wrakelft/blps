package ru.itmo.blps1.service.moderation;

import ru.itmo.blps1.dto.moderation.ModerationRequestResponse;
import ru.itmo.blps1.dto.moderation.RejectModerationRequest;
import ru.itmo.blps1.dto.moderation.SubmitBoardModerationResponse;
import ru.itmo.blps1.messaging.event.BoardModerationRequestEvent;

import java.util.List;

public interface BoardModerationServiceInt {

    SubmitBoardModerationResponse submitBoardForModeration(Long boardId);

    void processBoardModeration(BoardModerationRequestEvent event);

    void retryFailedExternalSync();

    List<ModerationRequestResponse> getAllModerationRequests();

    ModerationRequestResponse approveModerationRequest(Long requestId);

    ModerationRequestResponse rejectModerationRequest(Long requestId, RejectModerationRequest request);
}
