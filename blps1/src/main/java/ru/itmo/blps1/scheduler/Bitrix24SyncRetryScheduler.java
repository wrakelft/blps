package ru.itmo.blps1.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.itmo.blps1.service.moderation.BoardModerationServiceInt;

@Component
@RequiredArgsConstructor
@Slf4j
public class Bitrix24SyncRetryScheduler {

    private final BoardModerationServiceInt boardModerationService;

    @Scheduled(fixedDelayString = "${app.bitrix24.retry-delay-ms:30000}")
    public void retryFailedBitrix24Sync() {
        log.info("Starting scheduled retry for failed Bitrix24 sync");

        boardModerationService.retryFailedExternalSync();

        log.info("Finished scheduled retry for failed Bitrix24 sync");
    }
}
