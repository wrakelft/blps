package ru.itmo.blps1.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.itmo.blps1.dto.moderation.SubmitBoardModerationResponse;
import ru.itmo.blps1.service.moderation.BoardModerationServiceInt;

@RestController
@RequestMapping("/api/boards/{boardId}/moderation")
@RequiredArgsConstructor
public class BoardModerationController {

    private final BoardModerationServiceInt boardModerationService;

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SubmitBoardModerationResponse submitBoardForModeration(@PathVariable Long boardId) {
        return boardModerationService.submitBoardForModeration(boardId);
    }
}
