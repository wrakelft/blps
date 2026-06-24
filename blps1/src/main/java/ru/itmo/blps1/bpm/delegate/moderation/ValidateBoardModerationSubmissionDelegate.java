package ru.itmo.blps1.bpm.delegate.moderation;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.service.bpm.BoardModerationProcessActionService;

@Component("validateBoardModerationSubmissionDelegate")
@RequiredArgsConstructor
public class ValidateBoardModerationSubmissionDelegate implements JavaDelegate {

    private final BoardModerationProcessActionService actionService;

    @Override
    public void execute(DelegateExecution execution) {
        Long boardId = toLong(execution.getVariable("boardId"));

        BoardModerationProcessActionService.SubmissionData data =
                actionService.validateSubmission(boardId);

        execution.setVariable("boardId", data.boardId());
        execution.setVariable("requestedByUserId", data.requestedByUserId());
        execution.setVariable("requestedByUsername", data.requestedByUsername());
        execution.setVariable("requestedAt", data.requestedAt().toString());
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