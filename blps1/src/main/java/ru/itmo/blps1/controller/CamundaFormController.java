package ru.itmo.blps1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.blps1.dto.camunda.CamundaStartFormResponse;
import ru.itmo.blps1.dto.camunda.CamundaTaskFormResponse;
import ru.itmo.blps1.dto.camunda.SubmitModerationTaskFormRequest;
import ru.itmo.blps1.dto.camunda.SubmitModerationTaskFormResponse;
import ru.itmo.blps1.dto.camunda.SubmitStartFormRequest;
import ru.itmo.blps1.dto.camunda.SubmitStartFormResponse;
import ru.itmo.blps1.service.bpm.BusinessProcessService;

@RestController
@RequestMapping("/api/camunda/forms")
@RequiredArgsConstructor
public class CamundaFormController {

    private final BusinessProcessService businessProcessService;

    @GetMapping("/start/{processKey}")
    public CamundaStartFormResponse getStartForm(@PathVariable String processKey) {
        return businessProcessService.getStartForm(processKey);
    }

    @PostMapping("/start/{processKey}/submit")
    public SubmitStartFormResponse submitStartForm(
            @PathVariable String processKey,
            @Valid @RequestBody SubmitStartFormRequest request
    ) {
        return businessProcessService.submitStartForm(processKey, request);
    }

    @GetMapping("/tasks/{taskId}")
    public CamundaTaskFormResponse getTaskForm(@PathVariable String taskId) {
        return businessProcessService.getModerationTaskForm(taskId);
    }

    @PostMapping("/tasks/{taskId}/submit")
    public SubmitModerationTaskFormResponse submitTaskForm(
            @PathVariable String taskId,
            @Valid @RequestBody SubmitModerationTaskFormRequest request
    ) {
        return businessProcessService.submitModerationTaskForm(taskId, request);
    }
}