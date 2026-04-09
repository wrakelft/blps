package ru.itmo.blps1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.itmo.blps1.dto.board.BoardResponse;
import ru.itmo.blps1.dto.board.CreateBoardRequest;
import ru.itmo.blps1.service.BoardService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/api/boards")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardResponse createBoard(@Valid @RequestBody CreateBoardRequest request) {
        return boardService.createBoard(request);
    }

    @GetMapping("/api/boards")
    public List<BoardResponse> getAllBoards() {
        return boardService.getAllBoards();
    }

    @GetMapping("/api/boards/{id}")
    public BoardResponse getBoardById(@PathVariable Long id) {
        return boardService.getBoardById(id);
    }

    @GetMapping("/api/users/{userId}/boards")
    public List<BoardResponse> getBoardsByUserId(@PathVariable Long userId) {
        return boardService.getBoardsByUserId(userId);
    }
}
