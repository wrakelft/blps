package ru.itmo.blps1.bpm.delegate.scheduler;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.service.bpm.Bitrix24SyncRetryActionService;

@Component("retryFailedBitrixSyncDelegate")
@RequiredArgsConstructor
public class RetryFailedBitrixSyncDelegate implements JavaDelegate {

    private final Bitrix24SyncRetryActionService bitrix24SyncRetryActionService;

    @Override
    public void execute(DelegateExecution execution) {
        bitrix24SyncRetryActionService.retryFailedBitrix24Sync();
    }
}