package ru.itmo.blps1.bpm.delegate.moderation;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.service.bpm.BoardModerationProcessActionService;

@Component("createModerationRequestDelegate")
@RequiredArgsConstructor
public class CreateModerationRequestDelegate implements JavaDelegate {

    private final BoardModerationProcessActionService actionService;

    @Override
    public void execute(DelegateExecution execution) {
        Long boardId = toLong(execution.getVariable("boardId"));

        Long requestId = actionService.createModerationRequest(boardId);

        execution.setVariable("moderationRequestId", requestId);
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