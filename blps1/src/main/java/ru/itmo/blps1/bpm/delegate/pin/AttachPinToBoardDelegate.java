package ru.itmo.blps1.bpm.delegate.pin;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.dto.boardpin.BoardPinResponse;
import ru.itmo.blps1.service.boardpin.BoardPinServiceInt;

@Component("attachPinToBoardDelegate")
@RequiredArgsConstructor
public class AttachPinToBoardDelegate implements JavaDelegate {

    private final BoardPinServiceInt boardPinService;

    @Override
    public void execute(DelegateExecution execution) {
        Long boardId = toLong(execution.getVariable("boardId"));
        Long pinId = toLong(execution.getVariable("pinId"));

        BoardPinResponse response = boardPinService.saveExistingPinToBoard(boardId, pinId);

        execution.setVariable("boardPinId", response.getId());
        execution.setVariable("boardPinBoardId", response.getBoardId());
        execution.setVariable("boardPinPinId", response.getPinId());
        execution.setVariable(
                "boardPinSavedAt",
                response.getSavedAt() == null ? null : response.getSavedAt().toString()
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