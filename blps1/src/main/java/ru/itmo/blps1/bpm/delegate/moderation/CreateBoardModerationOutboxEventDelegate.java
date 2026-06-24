package ru.itmo.blps1.bpm.delegate.moderation;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.service.bpm.BoardModerationProcessActionService;

@Component("createBoardModerationOutboxEventDelegate")
@RequiredArgsConstructor
public class CreateBoardModerationOutboxEventDelegate implements JavaDelegate {

    private final BoardModerationProcessActionService actionService;

    @Override
    public void execute(DelegateExecution execution) {
        Long boardId = toLong(execution.getVariable("boardId"));
        Long requestedByUserId = toLong(execution.getVariable("requestedByUserId"));
        String requestedByUsername = (String) execution.getVariable("requestedByUsername");
        String requestedAt = (String) execution.getVariable("requestedAt");

        actionService.createOutboxEvent(
                boardId,
                requestedByUserId,
                requestedByUsername,
                requestedAt
        );
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