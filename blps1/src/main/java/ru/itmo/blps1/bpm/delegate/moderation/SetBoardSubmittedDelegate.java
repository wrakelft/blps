package ru.itmo.blps1.bpm.delegate.moderation;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.entity.enums.BoardModerationStatus;
import ru.itmo.blps1.service.bpm.BoardModerationProcessActionService;

@Component("setBoardSubmittedDelegate")
@RequiredArgsConstructor
public class SetBoardSubmittedDelegate implements JavaDelegate {

    private final BoardModerationProcessActionService actionService;

    @Override
    public void execute(DelegateExecution execution) {
        Long boardId = toLong(execution.getVariable("boardId"));

        BoardModerationStatus status = actionService.setSubmitted(boardId);

        execution.setVariable("moderationStatus", status.name());
        execution.setVariable("submitMessage", "Board moderation request accepted");
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