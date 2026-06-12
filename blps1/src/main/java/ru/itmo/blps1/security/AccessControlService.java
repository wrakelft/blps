package ru.itmo.blps1.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.entity.Board;
import ru.itmo.blps1.entity.Pin;
import ru.itmo.blps1.entity.enums.BoardModerationStatus;
import ru.itmo.blps1.entity.enums.BoardPrivacy;
import ru.itmo.blps1.exception.ForbiddenException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final CurrentUserService currentUserService;

    public void checkCanManageBoard(Board board) {
        if (currentUserService.isAdmin()) {
            return;
        }

        String currentUsername = currentUserService.getCurrentUsername();

        if (board.getOwner() == null || !currentUsername.equals(board.getOwner().getUsername())) {
            throw new ForbiddenException("You can manage only your own boards");
        }
    }

    public void checkCanViewBoard(Board board) {
        if (canViewBoard(board)) {
            return;
        }

        throw new ForbiddenException("You don't have permission to view this board");
    }

    public boolean canViewBoard(Board board) {
        if (board == null) {
            return false;
        }

        if (currentUserService.isAdmin()) {
            return true;
        }

        boolean isOwner = currentUserService.getCurrentUsernameOptional()
                .map(username -> board.getOwner() != null && username.equals(board.getOwner().getUsername()))
                .orElse(false);

        if(isOwner) {
            return true;
        }

        return board.getPrivacy() == BoardPrivacy.PUBLIC
                && board.getModerationStatus() == BoardModerationStatus.APPROVED;
    }

    public void checkCanViewPin(Pin pin, List<Board> boardsContainingPin) {
        if (canViewPin(pin, boardsContainingPin)) {
            return;
        }

        throw new ForbiddenException("You don't have permission to view this pin");
    }

    public boolean canViewPin(Pin pin, List<Board> boardsContainingPin) {
        if (pin == null) {
            return false;
        }

        if (currentUserService.isAdmin()) {
            return true;
        }

        boolean isAuthor = currentUserService.getCurrentUsernameOptional()
                .map(username -> pin.getAuthor() != null && username.equals(pin.getAuthor().getUsername()))
                .orElse(false);

        if (isAuthor) {
            return true;
        }

        return boardsContainingPin != null
                && boardsContainingPin.stream().anyMatch(this::canViewBoard);
    }
}
