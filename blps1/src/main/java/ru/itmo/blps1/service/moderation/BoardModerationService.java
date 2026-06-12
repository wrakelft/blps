package ru.itmo.blps1.service.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.blps1.dto.moderation.SubmitBoardModerationResponse;
import ru.itmo.blps1.entity.Board;
import ru.itmo.blps1.entity.BoardModerationRequest;
import ru.itmo.blps1.entity.User;
import ru.itmo.blps1.entity.enums.BoardModerationStatus;
import ru.itmo.blps1.entity.enums.ExternalSyncStatus;
import ru.itmo.blps1.entity.enums.ModerationRequestStatus;
import ru.itmo.blps1.exception.BadRequestException;
import ru.itmo.blps1.exception.NotFoundException;
import ru.itmo.blps1.messaging.event.BoardModerationRequestEvent;
import ru.itmo.blps1.messaging.producer.BoardModerationProducer;
import ru.itmo.blps1.repository.BoardModerationRequestRepository;
import ru.itmo.blps1.repository.BoardRepository;
import ru.itmo.blps1.security.AccessControlService;
import ru.itmo.blps1.security.CurrentUserService;
import ru.itmo.blps1.service.outbox.OutboxEventServiceInt;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class BoardModerationService implements BoardModerationServiceInt {

    private final BoardRepository boardRepository;

    private final BoardModerationRequestRepository boardModerationRequestRepository;

    private final AccessControlService accessControlService;

    private final CurrentUserService currentUserService;

    private final OutboxEventServiceInt outboxEventService;

    private final BoardModerationProducer boardModerationProducer;

    @Override
    @Transactional
    public SubmitBoardModerationResponse submitBoardForModeration(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found with id: " + boardId));

        accessControlService.checkCanManageBoard(board);

        BoardModerationStatus currentStatus = board.getModerationStatus();

        if (currentStatus == BoardModerationStatus.SUBMITTED || currentStatus == BoardModerationStatus.IN_REVIEW) {
            throw new BadRequestException("Board is already submitted for moderation");
        }

        if (currentStatus == BoardModerationStatus.APPROVED) {
            throw new BadRequestException("Approved board cannot be submitted for moderation again");
        }

        User currentUser = currentUserService.getCurrentUserEntity();

        board.setModerationStatus(BoardModerationStatus.SUBMITTED);
        Board savedBoard = boardRepository.save(board);

        BoardModerationRequestEvent event = new BoardModerationRequestEvent(
                savedBoard.getId(),
                currentUser.getId(),
                currentUser.getUsername(),
                OffsetDateTime.now()
        );

        outboxEventService.saveBoardModerationRequestedEvent(event);

        return SubmitBoardModerationResponse.builder()
                .boardId(savedBoard.getId())
                .moderationStatus(savedBoard.getModerationStatus())
                .message("Board moderation request accepted")
                .build();
    }

    @Override
    @Transactional
    public void processBoardModeration(BoardModerationRequestEvent event) {
        Board board = boardRepository.findById(event.boardId())
                .orElseThrow(() -> new NotFoundException("Board not found with id: " + event.boardId()));

        boardModerationRequestRepository.findFirstByBoardIdOrderByCreatedAtDesc(board.getId())
                .filter(existingRequest ->
                        existingRequest.getStatus() == ModerationRequestStatus.CREATED
                                || existingRequest.getStatus() == ModerationRequestStatus.IN_REVIEW
                                || existingRequest.getStatus() == ModerationRequestStatus.SENT_TO_EXTERNAL_SYSTEM
                )
                .ifPresent(existingRequest -> {
                    throw new BadRequestException("Active moderation request already exists for board: " + board.getId());
                });

        if (board.getModerationStatus() != BoardModerationStatus.SUBMITTED) {
            throw new BadRequestException(
                    "Board must be SUBMITTED status before moderation processing. Current status: "
                    + board.getModerationStatus()
            );
        }

        User requestedBy = board.getOwner();

        BoardModerationRequest request = BoardModerationRequest.builder()
                .board(board)
                .requestedBy(requestedBy)
                .status(ModerationRequestStatus.CREATED)
                .externalSyncStatus(ExternalSyncStatus.NOT_STARTED)
                .externalSystem("BITRIX24")
                .build();

        boardModerationRequestRepository.save(request);
        board.setModerationStatus(BoardModerationStatus.IN_REVIEW);
        boardRepository.save(board);

    }
}