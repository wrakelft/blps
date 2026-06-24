package ru.itmo.blps1.bpm.delegate.moderation;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.service.bpm.BoardModerationProcessActionService;

@Component("rejectBoardModerationDelegate")
@RequiredArgsConstructor
public class RejectBoardModerationDelegate implements JavaDelegate {

    private final BoardModerationProcessActionService actionService;

    @Override
    public void execute(DelegateExecution execution) {
        Long requestId = toLong(execution.getVariable("moderationRequestId"));
        String comment = (String) execution.getVariable("moderatorComment");

        BoardModerationProcessActionService.DecisionSyncData data =
                actionService.rejectModerationRequest(requestId, comment);

        execution.setVariable("boardId", data.boardId());
        execution.setVariable("externalTaskId", data.externalTaskId());
        execution.setVariable("decisionComment", data.decisionComment());
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