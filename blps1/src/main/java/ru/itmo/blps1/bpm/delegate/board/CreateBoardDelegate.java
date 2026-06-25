package ru.itmo.blps1.bpm.delegate.board;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.dto.board.BoardResponse;
import ru.itmo.blps1.dto.board.CreateBoardRequest;
import ru.itmo.blps1.entity.enums.BoardPrivacy;
import ru.itmo.blps1.service.board.BoardServiceInt;

@Component("createBoardDelegate")
@RequiredArgsConstructor
public class CreateBoardDelegate implements JavaDelegate {

    private final BoardServiceInt boardService;

    @Override
    public void execute(DelegateExecution execution) {
        CreateBoardRequest request = new CreateBoardRequest();

        request.setName((String) execution.getVariable("boardName"));
        request.setDescription((String) execution.getVariable("boardDescription"));
        request.setPrivacy(BoardPrivacy.valueOf((String) execution.getVariable("boardPrivacy")));

        BoardResponse response = boardService.createBoard(request);

        execution.setVariable("boardId", response.getId());
        execution.setVariable("createdBoardId", response.getId());
        execution.setVariable("createdBoardName", response.getName());
        execution.setVariable("createdBoardPrivacy", response.getPrivacy().toString());
    }
}