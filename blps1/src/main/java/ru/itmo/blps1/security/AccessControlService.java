package ru.itmo.blps1.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.entity.Board;
import ru.itmo.blps1.entity.enums.BoardPrivacy;
import ru.itmo.blps1.exception.ForbiddenException;

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
        if (board.getPrivacy() == BoardPrivacy.PUBLIC) {
            return true;
        }

        if (currentUserService.isAdmin()) {
            return true;
        }

        return currentUserService.getCurrentUsernameOptional()
                .map(username -> board.getOwner() != null && username.equals(board.getOwner().getUsername()))
                .orElse(false);
    }
}
