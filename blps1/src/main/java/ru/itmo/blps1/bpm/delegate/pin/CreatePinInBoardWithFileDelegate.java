package ru.itmo.blps1.bpm.delegate.pin;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.dto.boardpin.BoardPinResponse;
import ru.itmo.blps1.dto.boardpin.CreatePinInBoardResponse;
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.service.bpm.MultipartFileStore;
import ru.itmo.blps1.service.boardpin.BoardPinServiceInt;

@Component("createPinInBoardWithFileDelegate")
@RequiredArgsConstructor
public class CreatePinInBoardWithFileDelegate implements JavaDelegate {

    private final BoardPinServiceInt boardPinService;
    private final MultipartFileStore multipartFileStore;

    @Override
    public void execute(DelegateExecution execution) {
        Long boardId = toLong(execution.getVariable("boardId"));
        String title = (String) execution.getVariable("title");
        String description = (String) execution.getVariable("description");
        Long authorId = toLong(execution.getVariable("authorId"));
        String fileRef = (String) execution.getVariable("fileRef");

        MultipartFile file = multipartFileStore.get(fileRef);

        CreatePinInBoardResponse response = boardPinService.createPinInBoard(
                boardId,
                title,
                description,
                authorId,
                file
        );

        PinResponse pin = response.getPin();
        BoardPinResponse boardPin = response.getBoardPin();

        execution.setVariable("pinId", pin.getId());
        execution.setVariable("pinTitle", pin.getTitle());
        execution.setVariable("pinDescription", pin.getDescription());
        execution.setVariable("pinImageUrl", pin.getImageUrl());
        execution.setVariable("pinImageKey", pin.getImageKey());
        execution.setVariable("pinAuthorId", pin.getAuthorId());
        execution.setVariable(
                "pinCreatedAt",
                pin.getCreatedAt() == null ? null : pin.getCreatedAt().toString()
        );

        execution.setVariable("boardPinId", boardPin.getId());
        execution.setVariable("boardPinBoardId", boardPin.getBoardId());
        execution.setVariable("boardPinPinId", boardPin.getPinId());
        execution.setVariable(
                "boardPinSavedAt",
                boardPin.getSavedAt() == null ? null : boardPin.getSavedAt().toString()
        );
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long longValue) {
            return longValue;
        }

        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }

        return Long.valueOf(value.toString());
    }
}