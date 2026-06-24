package ru.itmo.blps1.bpm.delegate.scheduler;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.service.bpm.OutboxPublisherActionService;

@Component("publishOutboxEventsDelegate")
@RequiredArgsConstructor
public class PublishOutboxEventsDelegate implements JavaDelegate {

    private final OutboxPublisherActionService outboxPublisherActionService;

    @Override
    public void execute(DelegateExecution execution) {
        outboxPublisherActionService.publishOutboxEvents();
    }
}