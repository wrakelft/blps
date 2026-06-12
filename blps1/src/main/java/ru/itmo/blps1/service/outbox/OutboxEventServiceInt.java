package ru.itmo.blps1.service.outbox;

import ru.itmo.blps1.messaging.event.BoardModerationRequestEvent;

public interface OutboxEventServiceInt {

    void saveBoardModerationRequestedEvent(BoardModerationRequestEvent event);
}
