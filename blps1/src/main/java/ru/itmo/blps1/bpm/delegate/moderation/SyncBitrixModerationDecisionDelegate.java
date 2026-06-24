package ru.itmo.blps1.bpm.delegate.moderation;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.service.bpm.BoardModerationProcessActionService;

@Component("syncBitrixModerationDecisionDelegate")
@RequiredArgsConstructor
public class SyncBitrixModerationDecisionDelegate implements JavaDelegate {

    private final BoardModerationProcessActionService actionService;

    @Override
    public void execute(DelegateExecution execution) {
        Long requestId = toLong(execution.getVariable("moderationRequestId"));
        String externalTaskId = (String) execution.getVariable("externalTaskId");
        String decisionComment = (String) execution.getVariable("decisionComment");

        actionService.syncDecisionWithBitrix(requestId, externalTaskId, decisionComment);
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