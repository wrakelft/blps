package ru.itmo.blps1.service.bpm;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.blps1.dto.auth.LoginRequest;
import ru.itmo.blps1.dto.auth.LoginResponse;
import ru.itmo.blps1.dto.board.BoardResponse;
import ru.itmo.blps1.dto.board.CreateBoardRequest;
import ru.itmo.blps1.dto.board.UpdateBoardPrivacyRequest;
import ru.itmo.blps1.dto.boardpin.BoardPinResponse;
import ru.itmo.blps1.dto.boardpin.CreatePinInBoardResponse;
import ru.itmo.blps1.dto.camunda.CamundaModerationTaskResponse;
import ru.itmo.blps1.dto.camunda.CompleteModerationTaskRequest;
import ru.itmo.blps1.dto.camunda.CompleteModerationTaskResponse;
import ru.itmo.blps1.dto.file.FileUploadResponse;
import ru.itmo.blps1.dto.moderation.SubmitBoardModerationResponse;
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.dto.user.CreateUserRequest;
import ru.itmo.blps1.dto.user.UserResponse;
import ru.itmo.blps1.entity.enums.BoardModerationStatus;
import ru.itmo.blps1.entity.enums.BoardPrivacy;
import ru.itmo.blps1.exception.BadRequestException;
import ru.itmo.blps1.messaging.event.BoardModerationRequestEvent;
import ru.itmo.blps1.service.board.BoardServiceInt;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessProcessService {

    private static final String BOARD_CREATE_PROCESS_KEY = "board-create-process";
    private static final String BOARD_MODERATION_PROCESS_KEY = "board-moderation-process";
    private static final String BOARD_MODERATION_MESSAGE = "BoardModerationRequestedMessage";
    private static final String AUTH_LOGIN_PROCESS_KEY = "auth-login-process";
    private static final String USER_CREATE_PROCESS_KEY = "user-create-process";
    private static final String BOARD_PRIVACY_UPDATE_PROCESS_KEY = "board-privacy-update-process";
    private static final String BOARD_DELETE_PROCESS_KEY = "board-delete-process";
    private static final String FILE_UPLOAD_PROCESS_KEY = "file-upload-process";
    private static final String PIN_CREATE_WITH_FILE_PROCESS_KEY = "pin-create-with-file-process";
    private static final String BOARD_PIN_ATTACH_PROCESS_KEY = "board-pin-attach-process";
    private static final String PIN_CREATE_IN_BOARD_WITH_FILE_PROCESS_KEY = "pin-create-in-board-with-file-process";

    private final RuntimeService runtimeService;
    private final BoardServiceInt boardService;
    private final TaskService taskService;
    private final MultipartFileStore multipartFileStore;

    public BoardResponse createBoard(CreateBoardRequest request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("boardName", request.getName());
        variables.put("boardDescription", request.getDescription());
        variables.put("boardPrivacy", request.getPrivacy().name());
        variables.put("ownerId", request.getOwnerId());

        ProcessInstanceWithVariables result = runtimeService
                .createProcessInstanceByKey(BOARD_CREATE_PROCESS_KEY)
                .setVariables(variables)
                .executeWithVariablesInReturn();

        Long boardId = toLong(result.getVariables().get("boardId"));

        return boardService.getBoardById(boardId);
    }

    public SubmitBoardModerationResponse submitBoardForModeration(Long boardId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("boardId", boardId);

        ProcessInstanceWithVariables result = runtimeService
                .createProcessInstanceByKey(BOARD_MODERATION_PROCESS_KEY)
                .businessKey("board-" + boardId)
                .setVariables(variables)
                .executeWithVariablesInReturn();

        String moderationStatus = (String) result.getVariables().get("moderationStatus");
        String message = (String) result.getVariables().get("submitMessage");

        return SubmitBoardModerationResponse.builder()
                .boardId(boardId)
                .moderationStatus(BoardModerationStatus.valueOf(moderationStatus))
                .message(message)
                .build();
    }

    public void correlateBoardModerationRequested(BoardModerationRequestEvent event) {
        runtimeService
                .createMessageCorrelation(BOARD_MODERATION_MESSAGE)
                .processInstanceBusinessKey("board-" + event.boardId())
                .setVariable("boardId", event.boardId())
                .setVariable("requestedByUserId", event.requestedByUserId())
                .setVariable("requestedByUsername", event.requestedByUsername())
                .setVariable("requestedAt", event.requestedAt().toString())
                .correlate();
    }

    public List<CamundaModerationTaskResponse> getModerationTasks() {
        return taskService
                .createTaskQuery()
                .taskDefinitionKey("UserTask_AdminDecision")
                .orderByTaskCreateTime()
                .desc()
                .list()
                .stream()
                .map(this::toModerationTaskResponse)
                .toList();
    }

    public CompleteModerationTaskResponse completeModerationTask(
            String taskId,
            CompleteModerationTaskRequest request
    ) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskDefinitionKey("UserTask_AdminDecision")
                .singleResult();

        if (task == null) {
            throw new BadRequestException("Moderation task not found: " + taskId);
        }

        if ("REJECTED".equals(request.decision())
                && (request.comment() == null || request.comment().isBlank())) {
            throw new BadRequestException("Comment is required for rejection");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("moderationDecision", request.decision());
        variables.put("moderatorComment", request.comment());

        taskService.complete(taskId, variables);

        return new CompleteModerationTaskResponse(
                taskId,
                request.decision(),
                "Moderation task completed"
        );
    }

    public LoginResponse login(LoginRequest request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", request.getUsername());
        variables.put("password", request.getPassword());

        ProcessInstanceWithVariables result = runtimeService
                .createProcessInstanceByKey(AUTH_LOGIN_PROCESS_KEY)
                .setVariables(variables)
                .executeWithVariablesInReturn();

        String token = (String) result.getVariables().get("authToken");
        String username = (String) result.getVariables().get("authUsername");
        String rolesCsv = (String) result.getVariables().get("authRolesCsv");

        Set<String> roles = rolesCsv == null || rolesCsv.isBlank()
                ? Set.of()
                : Arrays.stream(rolesCsv.split(","))
                .filter(role -> !role.isBlank())
                .collect(Collectors.toSet());

        return new LoginResponse(token, username, roles);
    }

    public UserResponse createUser(CreateUserRequest request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("username", request.getUsername());
        variables.put("email", request.getEmail());

        ProcessInstanceWithVariables result = runtimeService
                .createProcessInstanceByKey(USER_CREATE_PROCESS_KEY)
                .setVariables(variables)
                .executeWithVariablesInReturn();

        return UserResponse.builder()
                .id(toLong(result.getVariables().get("userId")))
                .username((String) result.getVariables().get("createdUsername"))
                .email((String) result.getVariables().get("createdEmail"))
                .build();
    }

    private CamundaModerationTaskResponse toModerationTaskResponse(Task task) {
        Long boardId = toLong(taskService.getVariable(task.getId(), "boardId"));
        Long moderationRequestId = toLong(taskService.getVariable(task.getId(), "moderationRequestId"));

        LocalDateTime createdAt = task.getCreateTime() == null
                ? null
                : LocalDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneId.systemDefault());

        return new CamundaModerationTaskResponse(
                task.getId(),
                task.getName(),
                task.getAssignee(),
                task.getProcessInstanceId(),
                boardId,
                moderationRequestId,
                createdAt
        );
    }

    public BoardResponse updateBoardPrivacy(Long boardId, UpdateBoardPrivacyRequest request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("boardId", boardId);
        variables.put("boardPrivacy", request.getPrivacy().name());

        ProcessInstanceWithVariables result = runtimeService
                .createProcessInstanceByKey(BOARD_PRIVACY_UPDATE_PROCESS_KEY)
                .setVariables(variables)
                .executeWithVariablesInReturn();

        return BoardResponse.builder()
                .id(toLong(result.getVariables().get("updatedBoardId")))
                .name((String) result.getVariables().get("updatedBoardName"))
                .description((String) result.getVariables().get("updatedBoardDescription"))
                .privacy(BoardPrivacy.valueOf((String) result.getVariables().get("updatedBoardPrivacy")))
                .moderationStatus(BoardModerationStatus.valueOf((String) result.getVariables().get("updatedBoardModerationStatus")))
                .ownerId(toLong(result.getVariables().get("updatedBoardOwnerId")))
                .build();
    }

    public void deleteBoard(Long boardId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("boardId", boardId);

        runtimeService
                .createProcessInstanceByKey(BOARD_DELETE_PROCESS_KEY)
                .setVariables(variables)
                .executeWithVariablesInReturn();
    }

    public FileUploadResponse uploadFile(MultipartFile file) {
        String fileRef = multipartFileStore.put(file);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("fileRef", fileRef);

            ProcessInstanceWithVariables result = runtimeService
                    .createProcessInstanceByKey(FILE_UPLOAD_PROCESS_KEY)
                    .setVariables(variables)
                    .executeWithVariablesInReturn();

            return FileUploadResponse.builder()
                    .imageKey((String) result.getVariables().get("imageKey"))
                    .imageUrl((String) result.getVariables().get("imageUrl"))
                    .contentType((String) result.getVariables().get("contentType"))
                    .originalFileName((String) result.getVariables().get("originalFileName"))
                    .size(toLong(result.getVariables().get("fileSize")))
                    .build();
        } finally {
            multipartFileStore.remove(fileRef);
        }
    }

    public PinResponse createPinWithFile(
            String title,
            String description,
            Long authorId,
            MultipartFile file
    ) {
        String fileRef = multipartFileStore.put(file);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("title", title);
            variables.put("description", description);
            variables.put("authorId", authorId);
            variables.put("fileRef", fileRef);

            ProcessInstanceWithVariables result = runtimeService
                    .createProcessInstanceByKey(PIN_CREATE_WITH_FILE_PROCESS_KEY)
                    .setVariables(variables)
                    .executeWithVariablesInReturn();

            return buildPinResponse(result);
        } finally {
            multipartFileStore.remove(fileRef);
        }
    }

    public BoardPinResponse attachPinToBoard(Long boardId, Long pinId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("boardId", boardId);
        variables.put("pinId", pinId);

        ProcessInstanceWithVariables result = runtimeService
                .createProcessInstanceByKey(BOARD_PIN_ATTACH_PROCESS_KEY)
                .setVariables(variables)
                .executeWithVariablesInReturn();

        return buildBoardPinResponse(result);
    }

    public CreatePinInBoardResponse createPinInBoardWithFile(
            Long boardId,
            String title,
            String description,
            Long authorId,
            MultipartFile file
    ) {
        String fileRef = multipartFileStore.put(file);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("boardId", boardId);
            variables.put("title", title);
            variables.put("description", description);
            variables.put("authorId", authorId);
            variables.put("fileRef", fileRef);

            ProcessInstanceWithVariables result = runtimeService
                    .createProcessInstanceByKey(PIN_CREATE_IN_BOARD_WITH_FILE_PROCESS_KEY)
                    .setVariables(variables)
                    .executeWithVariablesInReturn();

            return CreatePinInBoardResponse.builder()
                    .pin(buildPinResponse(result))
                    .boardPin(buildBoardPinResponse(result))
                    .build();
        } finally {
            multipartFileStore.remove(fileRef);
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long longValue) {
            return longValue;
        }

        if (value instanceof Integer integerValue) {
            return integerValue.longValue();
        }

        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }

        return Long.valueOf(value.toString());
    }

    private PinResponse buildPinResponse(ProcessInstanceWithVariables result) {
        return PinResponse.builder()
                .id(toLong(result.getVariables().get("pinId")))
                .title((String) result.getVariables().get("pinTitle"))
                .description((String) result.getVariables().get("pinDescription"))
                .imageUrl((String) result.getVariables().get("pinImageUrl"))
                .imageKey((String) result.getVariables().get("pinImageKey"))
                .authorId(toLong(result.getVariables().get("pinAuthorId")))
                .createdAt(toOffsetDateTimeOrNull(result.getVariables().get("pinCreatedAt")))
                .build();
    }

    private BoardPinResponse buildBoardPinResponse(ProcessInstanceWithVariables result) {
        return BoardPinResponse.builder()
                .id(toLong(result.getVariables().get("boardPinId")))
                .boardId(toLong(result.getVariables().get("boardPinBoardId")))
                .pinId(toLong(result.getVariables().get("boardPinPinId")))
                .savedAt(toOffsetDateTimeOrNull(result.getVariables().get("boardPinSavedAt")))
                .build();
    }

    private OffsetDateTime toOffsetDateTimeOrNull(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime;
        }

        return OffsetDateTime.parse(value.toString());
    }
}