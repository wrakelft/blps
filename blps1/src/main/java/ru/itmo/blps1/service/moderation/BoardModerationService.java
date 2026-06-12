package ru.itmo.blps1.service.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.blps1.dto.moderation.ModerationRequestResponse;
import ru.itmo.blps1.dto.moderation.RejectModerationRequest;
import ru.itmo.blps1.dto.moderation.SubmitBoardModerationResponse;
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
import ru.itmo.blps1.mapper.BoardModerationRequestMapper;
import ru.itmo.blps1.messaging.event.BoardModerationRequestEvent;
import ru.itmo.blps1.repository.BoardModerationRequestRepository;
import ru.itmo.blps1.repository.BoardRepository;
import ru.itmo.blps1.security.AccessControlService;
import ru.itmo.blps1.security.CurrentUserService;
import ru.itmo.blps1.service.outbox.OutboxEventServiceInt;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardModerationService implements BoardModerationServiceInt {

    private final BoardRepository boardRepository;

    private final BoardModerationRequestRepository boardModerationRequestRepository;

    private final AccessControlService accessControlService;

    private final CurrentUserService currentUserService;

    private final OutboxEventServiceInt outboxEventService;

    private final CorporateModerationConnector corporateModerationConnector;

    private final BoardModerationRequestMapper boardModerationRequestMapper;

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


        board.setModerationStatus(BoardModerationStatus.IN_REVIEW);
        boardRepository.save(board);

    }

    @Override
    @Transactional
    public void retryFailedExternalSync() {
        List<BoardModerationRequest> failedRequests = boardModerationRequestRepository
                .findTop20ByExternalSyncStatusOrderByCreatedAtAsc(ExternalSyncStatus.FAILED);

        for (BoardModerationRequest request : failedRequests) {
            try {
                ExternalModerationTask externalTask = corporateModerationConnector.createModerationTask(request);

                request.setExternalSystem(externalTask.externalSystem());
                request.setExternalTaskId(externalTask.externalTaskId());
                request.setExternalSyncStatus(ExternalSyncStatus.SUCCESS);
                request.setStatus(ModerationRequestStatus.SENT_TO_EXTERNAL_SYSTEM);
                request.setComment(null);

                if (request.getBoard() != null) {
                    request.getBoard().setModerationStatus(BoardModerationStatus.IN_REVIEW);
                }
            } catch (Exception exception) {
                request.setComment("Retry failed to sync with Bitrix24: " + exception.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModerationRequestResponse> getAllModerationRequests() {
        return boardModerationRequestRepository.findAll()
                .stream()
                .map(boardModerationRequestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ModerationRequestResponse approveModerationRequest(Long requestId) {
        BoardModerationRequest request = boardModerationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Moderation request not found with id: " + requestId));

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

        return boardModerationRequestMapper.toResponse(request);
    }

    @Override
    @Transactional
    public ModerationRequestResponse rejectModerationRequest(Long requestId, RejectModerationRequest rejectRequest) {
        BoardModerationRequest request = boardModerationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Moderation request not found with id: " + requestId));

        if (request.getStatus() == ModerationRequestStatus.APPROVED) {
            throw new BadRequestException("Approved moderation request cannot be rejected");
        }

        if (request.getStatus() == ModerationRequestStatus.REJECTED) {
            throw new BadRequestException("Moderation request is already rejected");
        }

        User currentAdmin = currentUserService.getCurrentUserEntity();

        request.setStatus(ModerationRequestStatus.REJECTED);
        request.setModerator(currentAdmin);
        request.setProcessedAt(OffsetDateTime.now());
        request.setComment(rejectRequest.comment());

        request.getBoard().setModerationStatus(BoardModerationStatus.REJECTED);

        return boardModerationRequestMapper.toResponse(request);
    }
}