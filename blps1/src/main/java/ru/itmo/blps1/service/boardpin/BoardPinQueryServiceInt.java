package ru.itmo.blps1.service.boardpin;

import ru.itmo.blps1.entity.Board;

import java.util.List;

public interface BoardPinQueryServiceInt {

    List<Board> getBoardsContainingPin(Long pinId);
}
