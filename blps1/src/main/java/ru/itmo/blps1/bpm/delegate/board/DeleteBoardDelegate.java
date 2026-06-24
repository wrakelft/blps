package ru.itmo.blps1.bpm.delegate.board;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.service.board.BoardServiceInt;

@Component("deleteBoardDelegate")
@RequiredArgsConstructor
public class DeleteBoardDelegate implements JavaDelegate {

    private final BoardServiceInt boardService;

    @Override
    public void execute(DelegateExecution execution) {
        Long boardId = toLong(execution.getVariable("boardId"));

        boardService.deleteBoard(boardId);

        execution.setVariable("deletedBoardId", boardId);
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