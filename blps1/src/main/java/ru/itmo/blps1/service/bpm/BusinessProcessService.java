package ru.itmo.blps1.service.bpm;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.form.type.EnumFormType;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
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
import ru.itmo.blps1.dto.camunda.*;
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
    private final RepositoryService repositoryService;
    private final FormService formService;

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

    public CamundaStartFormResponse getStartForm(String processKey) {
        ProcessDefinition processDefinition = getLatestProcessDefinition(processKey);

        StartFormData formData = formService.getStartFormData(processDefinition.getId());

        List<CamundaFormFieldResponse> fields = formData.getFormFields()
                .stream()
                .map(this::toFormFieldResponse)
                .toList();

        return new CamundaStartFormResponse(
                processKey,
                processDefinition.getId(),
                formData.getFormKey(),
                fields
        );
    }

    public SubmitStartFormResponse submitStartForm(
            String processKey,
            SubmitStartFormRequest request
    ) {
        ProcessDefinition processDefinition = getLatestProcessDefinition(processKey);

        StartFormData formData = formService.getStartFormData(processDefinition.getId());

        Map<String, Object> convertedVariables = convertVariablesByFormFields(
                formData.getFormFields(),
                request.variables()
        );

        String businessKey = resolveBusinessKey(processKey, convertedVariables);

        ProcessInstance processInstance;

        if (businessKey == null) {
            processInstance = formService.submitStartForm(
                    processDefinition.getId(),
                    convertedVariables
            );
        } else {
            processInstance = formService.submitStartForm(
                    processDefinition.getId(),
                    businessKey,
                    convertedVariables
            );
        }

        return new SubmitStartFormResponse(
                processKey,
                processInstance.getProcessInstanceId(),
                businessKey,
                "Start form submitted"
        );
    }

    public CamundaTaskFormResponse getModerationTaskForm(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskDefinitionKey("UserTask_AdminDecision")
                .singleResult();

        if (task == null) {
            throw new BadRequestException("Moderation task not found: " + taskId);
        }

        TaskFormData formData = formService.getTaskFormData(taskId);

        List<CamundaFormFieldResponse> fields = formData.getFormFields()
                .stream()
                .map(this::toFormFieldResponse)
                .toList();

        return new CamundaTaskFormResponse(
                taskId,
                task.getName(),
                formData.getFormKey(),
                fields
        );
    }

    public SubmitModerationTaskFormResponse submitModerationTaskForm(
            String taskId,
            SubmitModerationTaskFormRequest request
    ) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskDefinitionKey("UserTask_AdminDecision")
                .singleResult();

        if (task == null) {
            throw new BadRequestException("Moderation task not found: " + taskId);
        }

        if ("REJECTED".equals(request.moderationDecision())
                && (request.moderatorComment() == null || request.moderatorComment().isBlank())) {
            throw new BadRequestException("Comment is required for rejection");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("moderationDecision", request.moderationDecision());
        variables.put("moderatorComment", request.moderatorComment());

        formService.submitTaskForm(taskId, variables);

        return new SubmitModerationTaskFormResponse(
                taskId,
                request.moderationDecision(),
                "Moderation task form submitted"
        );
    }

    private ProcessDefinition getLatestProcessDefinition(String processKey) {
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .singleResult();

        if (processDefinition == null) {
            throw new BadRequestException("Process definition not found: " + processKey);
        }

        return processDefinition;
    }

    private CamundaFormFieldResponse toFormFieldResponse(FormField field) {
        Map<String, String> values = Collections.emptyMap();

        if (field.getType() instanceof EnumFormType enumFormType) {
            values = enumFormType.getValues();
        }

        return new CamundaFormFieldResponse(
                field.getId(),
                field.getLabel(),
                field.getType() == null ? null : field.getType().getName(),
                field.getDefaultValue(),
                values
        );
    }

    private Map<String, Object> convertVariablesByFormFields(
            List<FormField> formFields,
            Map<String, Object> rawVariables
    ) {
        Map<String, Object> converted = new LinkedHashMap<>();

        for (FormField field : formFields) {
            Object rawValue = rawVariables.get(field.getId());

            if (rawValue == null) {
                if (field.getDefaultValue() != null) {
                    converted.put(field.getId(), field.getDefaultValue());
                }
                continue;
            }

            String typeName = field.getType() == null ? "string" : field.getType().getName();

            converted.put(field.getId(), convertFormValue(typeName, rawValue));
        }

        return converted;
    }

    private Object convertFormValue(String typeName, Object rawValue) {
        if (rawValue == null) {
            return null;
        }

        return switch (typeName) {
            case "long" -> toLong(rawValue);
            case "boolean" -> toBoolean(rawValue);
            default -> rawValue.toString();
        };
    }

    private Boolean toBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        return Boolean.valueOf(value.toString());
    }

    private String resolveBusinessKey(String processKey, Map<String, Object> variables) {
        if ("board-moderation-process".equals(processKey)) {
            Object boardId = variables.get("boardId");
            return boardId == null ? null : "board-" + boardId;
        }

        if ("board-privacy-update-process".equals(processKey)) {
            Object boardId = variables.get("boardId");
            return boardId == null ? null : "board-" + boardId;
        }

        if ("board-create-process".equals(processKey)) {
            Object ownerId = variables.get("ownerId");
            return ownerId == null ? null : "owner-" + ownerId + "-board-create";
        }

        return null;
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