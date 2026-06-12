package ru.itmo.blps1.integration.corporate;

import ru.itmo.blps1.entity.BoardModerationRequest;
import ru.itmo.blps1.integration.corporate.dto.ExternalModerationTask;

public interface CorporateModerationConnector {

    ExternalModerationTask createModerationTask(BoardModerationRequest request);

    void addDecisionComment(String externalTaskId, String comment);

    void completeTask(String externalTaskId);
}
