package ru.itmo.blps1.integration.bitrix24.jca;

import jakarta.resource.ResourceException;
import org.springframework.web.client.RestClient;
import ru.itmo.blps1.entity.Board;
import ru.itmo.blps1.entity.BoardModerationRequest;
import ru.itmo.blps1.integration.corporate.dto.ExternalModerationTask;

import java.util.Map;

public class Bitrix24Connection implements AutoCloseable {

    private static final String EXTERNAL_SYSTEM = "BITRIX24";

    private final Bitrix24ManagedConnection managedConnection;

    private final RestClient restClient;

    private final String webhookUrl;

    private final Long responsibleId;

    private boolean closed;

    public Bitrix24Connection(
            Bitrix24ManagedConnection managedConnection,
            RestClient restClient,
            String webhookUrl,
            Long responsibleId
    ) {
        this.managedConnection = managedConnection;
        this.restClient = restClient;
        this.webhookUrl = normalizeWebhookUrl(webhookUrl);
        this.responsibleId = responsibleId;
    }

    public ExternalModerationTask createTask(BoardModerationRequest request) {
        checkOpen();

        if (webhookUrl == null || webhookUrl.isBlank()) {
            String mockId = "MOCK-BITRIX-TASK-" + request.getId();
            return new ExternalModerationTask(mockId, EXTERNAL_SYSTEM);
        }

        Board board = request.getBoard();

        String title = "Moderation request for board: " + board.getName();

        String description = """
                Board moderation request
                
                Request ID: %d
                Board ID: %d
                Board name: %s
                Board privacy: %s
                Owner ID: %d
                Owner username: %s
                """.formatted(
                request.getId(),
                board.getId(),
                board.getName(),
                board.getPrivacy(),
                board.getOwner().getId(),
                board.getOwner().getUsername()
        );

        Map<String, Object> body = Map.of(
                "fields", Map.of(
                        "TITLE", title,
                        "DESCRIPTION", description,
                        "RESPONSIBLE_ID", responsibleId
                )
        );

        Bitrix24TaskAddResponse response = restClient.post()
                .uri(webhookUrl + "tasks.task.add")
                .body(body)
                .retrieve()
                .body(Bitrix24TaskAddResponse.class);

        if (response == null || response.result() == null || response.result().task() == null) {
            throw new IllegalStateException("Bitrix24 returned empty task creation response");
        }

        Long taskId = response.result().task().id();

        return new ExternalModerationTask(String.valueOf(taskId), EXTERNAL_SYSTEM);
    }

    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException("Bitrix24 connection is closed");
        }
    }

    private String normalizeWebhookUrl(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        return value.endsWith("/") ? value : value + "/";
    }

    public void addComment(String externalTaskId, String comment) {
        checkOpen();

        if (externalTaskId == null || externalTaskId.isBlank()) {
            return;
        }

        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        Map<String, Object> body = Map.of(
                "TASKID", Long.valueOf(externalTaskId),
                "FIELDS", Map.of(
                        "POST_MESSAGE", comment
                )
        );

        restClient.post()
                .uri(webhookUrl + "task.commentitem.add")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void completeTask(String externalTaskId) {
        checkOpen();

        if (externalTaskId == null || externalTaskId.isBlank()) {
            return;
        }

        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        Map<String, Object> body = Map.of(
                "taskId", Long.valueOf(externalTaskId)
        );

        restClient.post()
                .uri(webhookUrl + "tasks.task.complete")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void close() {
        this.closed = true;
    }

    public record Bitrix24TaskAddResponse(
            Bitrix24TaskAddResult result
    ) {
    }

    public record Bitrix24TaskAddResult(
            Bitrix24Task task
    ) {
    }

    public record Bitrix24Task(
            Long id
    ) {
    }
}