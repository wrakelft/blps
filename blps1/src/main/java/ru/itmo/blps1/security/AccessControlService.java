package ru.itmo.blps1.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.entity.Board;
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
}
