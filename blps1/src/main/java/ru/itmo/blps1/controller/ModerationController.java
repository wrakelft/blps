package ru.itmo.blps1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.blps1.dto.moderation.ModerationRequestResponse;
import ru.itmo.blps1.dto.moderation.RejectModerationRequest;
import ru.itmo.blps1.service.moderation.BoardModerationServiceInt;

import java.util.List;

@RestController
@RequestMapping("/api/moderation/requests")
@RequiredArgsConstructor
public class ModerationController {

    private final BoardModerationServiceInt boardModerationService;

    @GetMapping
    public List<ModerationRequestResponse> getAllModerationRequests() {
        return boardModerationService.getAllModerationRequests();
    }

    @PostMapping("/{requestId}/approve")
    public ModerationRequestResponse approveModerationRequest(@PathVariable Long requestId) {
        return boardModerationService.approveModerationRequest(requestId);
    }

    @PostMapping("/{requestId}/reject")
    public ModerationRequestResponse rejectModerationRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody RejectModerationRequest request
    ) {
        return boardModerationService.rejectModerationRequest(requestId, request);
    }
}
