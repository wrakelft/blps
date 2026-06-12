package ru.itmo.blps1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps1.entity.BoardModerationRequest;
import ru.itmo.blps1.entity.enums.ExternalSyncStatus;
import ru.itmo.blps1.entity.enums.ModerationRequestStatus;

import java.util.List;
import java.util.Optional;

public interface BoardModerationRequestRepository extends JpaRepository<BoardModerationRequest, Long> {

    Optional<BoardModerationRequest> findFirstByBoardIdOrderByCreatedAtDesc(Long boardId);

    List<BoardModerationRequest> findAllByStatus(ModerationRequestStatus status);

    List<BoardModerationRequest> findAllByExternalSyncStatus(ExternalSyncStatus externalSyncStatus);

    List<BoardModerationRequest> findTop20ByExternalSyncStatusOrderByCreatedAtAsc(
            ExternalSyncStatus externalSyncStatus
    );
}
