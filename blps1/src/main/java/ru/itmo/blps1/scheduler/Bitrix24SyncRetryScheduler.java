package ru.itmo.blps1.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.service.bpm.Bitrix24SyncRetryActionService;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.bitrix24.retry-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class Bitrix24SyncRetryScheduler {

    private final Bitrix24SyncRetryActionService bitrix24SyncRetryActionService;

    @Scheduled(fixedDelayString = "${app.bitrix24.retry-delay-ms:30000}")
    public void retryFailedBitrix24Sync() {
        bitrix24SyncRetryActionService.retryFailedBitrix24Sync();
    }
}