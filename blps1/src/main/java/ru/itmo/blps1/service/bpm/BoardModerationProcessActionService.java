package ru.itmo.blps1.service.bpm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.blps1.entity.Board;
import ru.itmo.blps1.entity.BoardModerationRequest;
import ru.itmo.blps1.entity.User;
import ru.itmo.blps1.entity.enums.BoardModerationStatus;
import ru.itmo.blps1.entity.enums.ExternalSyncStatus;
import ru.itmo.blps1.entity.enums.ModerationRequestStatus;
import ru.itmo.blps1.exception.BadRequestException;
import ru.itmo.blps1.exception.NotFoundException;
import ru.itmo.blps1.integration.corporate.CorporateModerationConnector;
import ru.itmo.blps1.integration.corporate.dto.ExternalModerationTask;
import ru.itmo.blps1.messaging.event.BoardModerationRequestEvent;
import ru.itmo.blps1.repository.BoardModerationRequestRepository;
import ru.itmo.blps1.repository.BoardRepository;
import ru.itmo.blps1.security.AccessControlService;
import ru.itmo.blps1.security.CurrentUserService;
import ru.itmo.blps1.service.outbox.OutboxEventServiceInt;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class BoardModerationProcessActionService {

    private final BoardRepository boardRepository;
    private final BoardModerationRequestRepository boardModerationRequestRepository;
    private final AccessControlService accessControlService;
    private final CurrentUserService currentUserService;
    private final OutboxEventServiceInt outboxEventService;
    private final CorporateModerationConnector corporateModerationConnector;

    @Transactional(readOnly = true)
    public SubmissionData validateSubmission(Long boardId) {
        Board board = getBoard(boardId);

        accessControlService.checkCanManageBoard(board);

        BoardModerationStatus currentStatus = board.getModerationStatus();

        if (currentStatus == BoardModerationStatus.SUBMITTED || currentStatus == BoardModerationStatus.IN_REVIEW) {
            throw new BadRequestException("Board is already submitted for moderation");
        }

        if (currentStatus == BoardModerationStatus.APPROVED) {
            throw new BadRequestException("Approved board cannot be submitted for moderation again");
        }

        User currentUser = currentUserService.getCurrentUserEntity();

        return new SubmissionData(
                board.getId(),
                currentUser.getId(),
                currentUser.getUsername(),
                OffsetDateTime.now()
        );
    }

    @Transactional
    public BoardModerationStatus setSubmitted(Long boardId) {
        Board board = getBoard(boardId);
        board.setModerationStatus(BoardModerationStatus.SUBMITTED);
        return boardRepository.save(board).getModerationStatus();
    }

    @Transactional
    public void createOutboxEvent(Long boardId, Long requestedByUserId, String requestedByUsername, String requestedAt) {
        BoardModerationRequestEvent event = new BoardModerationRequestEvent(
                boardId,
                requestedByUserId,
                requestedByUsername,
                OffsetDateTime.parse(requestedAt)
        );

        outboxEventService.saveBoardModerationRequestedEvent(event);
    }

    @Transactional
    public Long createModerationRequest(Long boardId) {
        Board board = getBoard(boardId);

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

        BoardModerationRequest request = BoardModerationRequest.builder()
                .board(board)
                .requestedBy(board.getOwner())
                .status(ModerationRequestStatus.CREATED)
                .externalSyncStatus(ExternalSyncStatus.NOT_STARTED)
                .externalSystem("BITRIX24")
                .build();

        BoardModerationRequest savedRequest = boardModerationRequestRepository.save(request);

        return savedRequest.getId();
    }

    @Transactional
    public void createBitrixTask(Long requestId) {
        BoardModerationRequest request = boardModerationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Moderation request not found with id: " + requestId));

        try {
            ExternalModerationTask externalTask = corporateModerationConnector.createModerationTask(request);

            request.setExternalSystem(externalTask.externalSystem());
            request.setExternalTaskId(externalTask.externalTaskId());
            request.setExternalSyncStatus(ExternalSyncStatus.SUCCESS);
            request.setStatus(ModerationRequestStatus.SENT_TO_EXTERNAL_SYSTEM);
        } catch (Exception exception) {
            request.setExternalSyncStatus(ExternalSyncStatus.FAILED);
            request.setStatus(ModerationRequestStatus.FAILED);
            request.setComment("Failed to sync with Bitrix24: " + exception.getMessage());
        }

        request.getBoard().setModerationStatus(BoardModerationStatus.IN_REVIEW);
    }

    @Transactional
    public DecisionSyncData approveModerationRequest(Long requestId) {
        BoardModerationRequest request = getModerationRequest(requestId);

        if (request.getStatus() == ModerationRequestStatus.APPROVED) {
            throw new BadRequestException("Moderation request is already approved");
        }

        if (request.getStatus() == ModerationRequestStatus.REJECTED) {
            throw new BadRequestException("Rejected moderation request cannot be approved");
        }

        User currentAdmin = currentUserService.getCurrentUserEntity();

        request.setStatus(ModerationRequestStatus.APPROVED);
        request.setModerator(currentAdmin);
        request.setProcessedAt(OffsetDateTime.now());
        request.setComment(null);

        request.getBoard().setModerationStatus(BoardModerationStatus.APPROVED);

        String decisionComment = "Board moderation approved. Board ID: " + request.getBoard().getId();

        return new DecisionSyncData(
                request.getId(),
                request.getBoard().getId(),
                request.getExternalTaskId(),
                decisionComment
        );
    }

    @Transactional
    public DecisionSyncData rejectModerationRequest(Long requestId, String comment) {
        BoardModerationRequest request = getModerationRequest(requestId);

        if (request.getStatus() == ModerationRequestStatus.APPROVED) {
            throw new BadRequestException("Approved moderation request cannot be rejected");
        }

        if (request.getStatus() == ModerationRequestStatus.REJECTED) {
            throw new BadRequestException("Moderation request is already rejected");
        }

        if (comment == null || comment.isBlank()) {
            throw new BadRequestException("Reject comment is required");
        }

        User currentAdmin = currentUserService.getCurrentUserEntity();

        request.setStatus(ModerationRequestStatus.REJECTED);
        request.setModerator(currentAdmin);
        request.setProcessedAt(OffsetDateTime.now());
        request.setComment(comment);

        request.getBoard().setModerationStatus(BoardModerationStatus.REJECTED);

        String decisionComment = "Board moderation rejected. Board ID: "
                + request.getBoard().getId()
                + ". Reason: "
                + comment;

        return new DecisionSyncData(
                request.getId(),
                request.getBoard().getId(),
                request.getExternalTaskId(),
                decisionComment
        );
    }

    @Transactional
    public void syncDecisionWithBitrix(Long requestId, String externalTaskId, String decisionComment) {
        if (externalTaskId == null || externalTaskId.isBlank()) {
            return;
        }

        BoardModerationRequest request = getModerationRequest(requestId);

        try {
            corporateModerationConnector.addDecisionComment(externalTaskId, decisionComment);
            corporateModerationConnector.completeTask(externalTaskId);
        } catch (Exception exception) {
            request.setComment(
                    appendComment(
                            request.getComment(),
                            "Failed to update Bitrix24 task after decision: " + exception.getMessage()
                    )
            );
        }
    }

    private BoardModerationRequest getModerationRequest(Long requestId) {
        return boardModerationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Moderation request not found with id: " + requestId));
    }

    private String appendComment(String currentComment, String newComment) {
        if (currentComment == null || currentComment.isBlank()) {
            return newComment;
        }

        return currentComment + "\n" + newComment;
    }

    public record DecisionSyncData(
            Long moderationRequestId,
            Long boardId,
            String externalTaskId,
            String decisionComment
    ) {
    }

    private Board getBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board not found with id: " + boardId));
    }

    public record SubmissionData(
            Long boardId,
            Long requestedByUserId,
            String requestedByUsername,
            OffsetDateTime requestedAt
    ) {
    }
}