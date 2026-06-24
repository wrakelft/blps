package ru.itmo.blps1.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.service.bpm.OutboxPublisherActionService;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.outbox.publisher-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class OutboxPublisherScheduler {

    private final OutboxPublisherActionService outboxPublisherActionService;

    @Scheduled(fixedDelayString = "${app.outbox.publish-delay-ms:5000}")
    public void publishOutboxEvents() {
        outboxPublisherActionService.publishOutboxEvents();
    }
}