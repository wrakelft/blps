package ru.itmo.blps1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.blps1.dto.camunda.CamundaModerationTaskResponse;
import ru.itmo.blps1.dto.camunda.CompleteModerationTaskRequest;
import ru.itmo.blps1.dto.camunda.CompleteModerationTaskResponse;
import ru.itmo.blps1.service.bpm.BusinessProcessService;

import java.util.List;

@RestController
@RequestMapping("/api/camunda/tasks")
@RequiredArgsConstructor
public class CamundaTaskController {

    private final BusinessProcessService businessProcessService;

    @GetMapping("/moderation")
    public List<CamundaModerationTaskResponse> getModerationTasks() {
        return businessProcessService.getModerationTasks();
    }

    @PostMapping("/{taskId}/complete-moderation")
    public CompleteModerationTaskResponse completeModerationTask(
            @PathVariable String taskId,
            @Valid @RequestBody CompleteModerationTaskRequest request
    ) {
        return businessProcessService.completeModerationTask(taskId, request);
    }
}