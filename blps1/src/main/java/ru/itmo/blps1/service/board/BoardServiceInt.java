package ru.itmo.blps1.service.board;

import ru.itmo.blps1.dto.board.BoardResponse;
import ru.itmo.blps1.dto.board.CreateBoardRequest;
import ru.itmo.blps1.entity.Board;

import java.util.List;

public interface BoardServiceInt {

    BoardResponse createBoard(CreateBoardRequest request);

    List<BoardResponse> getAllBoards();

    BoardResponse getBoardById(Long id);

    List<BoardResponse> getBoardsByUserId(Long userId);

    Board getBoardEntityById(Long id);
}
