package ru.itmo.blps1.service.moderation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.blps1.dto.moderation.SubmitBoardModerationResponse;
import ru.itmo.blps1.entity.Board;
import ru.itmo.blps1.entity.enums.BoardModerationStatus;
import ru.itmo.blps1.exception.BadRequestException;
import ru.itmo.blps1.exception.NotFoundException;
import ru.itmo.blps1.repository.BoardRepository;
import ru.itmo.blps1.security.AccessControlService;

@Service
@RequiredArgsConstructor
public class BoardModerationService implements BoardModerationServiceInt {

    private final BoardRepository boardRepository;

    private final AccessControlService accessControlService;

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

        board.setModerationStatus(BoardModerationStatus.SUBMITTED);
        Board savedBoard = boardRepository.save(board);

        return SubmitBoardModerationResponse.builder()
                .boardId(savedBoard.getId())
                .moderationStatus(savedBoard.getModerationStatus())
                .message("Board moderation request accepted")
                .build();
    }
}