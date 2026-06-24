package ru.itmo.blps1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.itmo.blps1.dto.board.BoardResponse;
import ru.itmo.blps1.dto.board.CreateBoardRequest;
import ru.itmo.blps1.dto.board.UpdateBoardPrivacyRequest;
import ru.itmo.blps1.service.board.BoardServiceInt;
import ru.itmo.blps1.service.bpm.BusinessProcessService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Boards", description = "Operations with boards")
public class BoardController {

    private final BoardServiceInt boardService;
    private final BusinessProcessService businessProcessService;

    @PostMapping("/api/boards")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create board")
    public BoardResponse createBoard(@Valid @RequestBody CreateBoardRequest request) {
        return businessProcessService.createBoard(request);
    }

    @GetMapping("/api/boards")
    @Operation(summary = "Get all boards")
    public List<BoardResponse> getAllBoards() {
        return boardService.getAllBoards();
    }

    @GetMapping("/api/boards/{id}")
    @Operation(summary = "Get board by id")
    public BoardResponse getBoardById(@PathVariable Long id) {
        return boardService.getBoardById(id);
    }

    @GetMapping("/api/users/{userId}/boards")
    @Operation(summary = "Get board by user id")
    public List<BoardResponse> getBoardsByUserId(@PathVariable Long userId) {
        return boardService.getBoardsByUserId(userId);
    }

    @PatchMapping("/api/boards/{id}/privacy")
    @Operation(summary = "Update board privacy")
    public BoardResponse updateBoardPrivacy(@PathVariable Long id,
                                            @Valid @RequestBody UpdateBoardPrivacyRequest request) {
        return boardService.updateBoardPrivacy(id, request);
    }

    @DeleteMapping("/api/boards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete board with all board-pin links")
    public void deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
    }
}
