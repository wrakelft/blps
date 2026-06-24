package ru.itmo.blps1.service.bpm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.service.moderation.BoardModerationServiceInt;

@Service
@RequiredArgsConstructor
@Slf4j
public class Bitrix24SyncRetryActionService {

    private final BoardModerationServiceInt boardModerationService;

    public void retryFailedBitrix24Sync() {
        log.info("Starting retry for failed Bitrix24 sync");

        boardModerationService.retryFailedExternalSync();

        log.info("Finished retry for failed Bitrix24 sync");
    }
}