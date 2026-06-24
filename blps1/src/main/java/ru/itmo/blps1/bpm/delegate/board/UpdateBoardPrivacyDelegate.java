package ru.itmo.blps1.bpm.delegate.board;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.dto.board.BoardResponse;
import ru.itmo.blps1.dto.board.UpdateBoardPrivacyRequest;
import ru.itmo.blps1.entity.enums.BoardPrivacy;
import ru.itmo.blps1.service.board.BoardServiceInt;

@Component("updateBoardPrivacyDelegate")
@RequiredArgsConstructor
public class UpdateBoardPrivacyDelegate implements JavaDelegate {

    private final BoardServiceInt boardService;

    @Override
    public void execute(DelegateExecution execution) {
        Long boardId = toLong(execution.getVariable("boardId"));
        BoardPrivacy privacy = BoardPrivacy.valueOf((String) execution.getVariable("boardPrivacy"));

        UpdateBoardPrivacyRequest request = new UpdateBoardPrivacyRequest();
        request.setPrivacy(privacy);

        BoardResponse response = boardService.updateBoardPrivacy(boardId, request);

        execution.setVariable("updatedBoardId", response.getId());
        execution.setVariable("updatedBoardName", response.getName());
        execution.setVariable("updatedBoardDescription", response.getDescription());
        execution.setVariable("updatedBoardPrivacy", response.getPrivacy().name());
        execution.setVariable("updatedBoardModerationStatus", response.getModerationStatus().name());
        execution.setVariable("updatedBoardOwnerId", response.getOwnerId());
    }

    private Long toLong(Object value) {
        if (value instanceof Long longValue) {
            return longValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }

        return Long.valueOf(value.toString());
    }
}