package ru.itmo.blps1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.itmo.blps1.dto.boardpin.BoardPinResponse;
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.service.BoardPinService;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardPinController {

    private final BoardPinService boardPinService;

    @PostMapping("/{boardId}/pins/{pinId}")
    @ResponseStatus(HttpStatus.CREATED)
    public BoardPinResponse saveExistingPinToBoard(
            @PathVariable Long boardId,
            @PathVariable Long pinId
    ) {
        return boardPinService.saveExistingPinToBoard(boardId, pinId);
    }

    @GetMapping("/{boardId}/pins")
    public List<PinResponse> getPinsByBoardId(@PathVariable Long boardId) {
        return boardPinService.getPinsByBoardId(boardId);
    }
}