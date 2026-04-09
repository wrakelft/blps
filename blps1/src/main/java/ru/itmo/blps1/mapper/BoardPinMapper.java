package ru.itmo.blps1.mapper;

import org.springframework.stereotype.Component;
import ru.itmo.blps1.dto.boardpin.BoardPinResponse;
import ru.itmo.blps1.entity.BoardPin;

@Component
public class BoardPinMapper {

    public BoardPinResponse toResponse(BoardPin boardPin) {
        if (boardPin == null) {
            return null;
        }

        return BoardPinResponse.builder()
                .id(boardPin.getId())
                .boardId(boardPin.getBoard() != null ? boardPin.getBoard().getId() : null)
                .pinId(boardPin.getPin() != null ? boardPin.getPin().getId() : null)
                .savedAt(boardPin.getSavedAt())
                .build();
    }
}