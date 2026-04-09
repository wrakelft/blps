package ru.itmo.blps1.mapper;

import org.springframework.stereotype.Component;
import ru.itmo.blps1.dto.board.BoardResponse;
import ru.itmo.blps1.entity.Board;

@Component
public class BoardMapper {

    public BoardResponse toResponse(Board board) {
        if (board == null) {
            return null;
        }

        return BoardResponse.builder()
                .id(board.getId())
                .name(board.getName())
                .description(board.getDescription())
                .privacy(board.getPrivacy())
                .createdAt(board.getCreatedAt())
                .ownerId(board.getOwner() != null ? board.getOwner().getId() : null)
                .build();
    }
}