package ru.itmo.blps1.service.board;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.blps1.dto.board.BoardResponse;
import ru.itmo.blps1.dto.board.CreateBoardRequest;
import ru.itmo.blps1.dto.board.UpdateBoardPrivacyRequest;
import ru.itmo.blps1.entity.Board;
import ru.itmo.blps1.entity.User;
import ru.itmo.blps1.exception.ConflictException;
import ru.itmo.blps1.exception.NotFoundException;
import ru.itmo.blps1.mapper.BoardMapper;
import ru.itmo.blps1.repository.BoardRepository;
import ru.itmo.blps1.security.AccessControlService;
import ru.itmo.blps1.security.CurrentUserService;
import ru.itmo.blps1.service.user.UserServiceInt;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService implements BoardServiceInt {

    private final BoardRepository boardRepository;
    private final UserServiceInt userService;
    private final CurrentUserService currentUserService;
    private final AccessControlService accessControlService;
    private final BoardMapper boardMapper;

    @Override
    @Transactional
    public BoardResponse createBoard(CreateBoardRequest request) {
        User owner = currentUserService.getCurrentUserEntity();

        if (boardRepository.existsByOwnerIdAndName(owner.getId(), request.getName())) {
            throw new ConflictException("Board with this name already exists for the user");
        }

        Board board = Board.builder()
                .name(request.getName())
                .description(request.getDescription())
                .privacy(request.getPrivacy())
                .owner(owner)
                .build();

        Board savedBoard = boardRepository.save(board);
        return boardMapper.toResponse(savedBoard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardResponse> getAllBoards() {
        return boardRepository.findAll()
                .stream()
                .filter(accessControlService::canViewBoard)
                .map(boardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BoardResponse getBoardById(Long id) {
        Board board = getBoardEntityById(id);

        accessControlService.checkCanViewBoard(board);

        return boardMapper.toResponse(board);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardResponse> getBoardsByUserId(Long userId) {
        return boardRepository.findAllByOwnerId(userId)
                .stream()
                .filter(accessControlService::canViewBoard)
                .map(boardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Board getBoardEntityById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Board with id " + id + " not found"));
    }

    @Override
    @Transactional
    public BoardResponse updateBoardPrivacy(Long id, UpdateBoardPrivacyRequest request) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Board with id " + id + " not found"));

        accessControlService.checkCanManageBoard(board);

        board.setPrivacy(request.getPrivacy());

        Board savedBoard = boardRepository.save(board);
        return boardMapper.toResponse(savedBoard);
    }

    @Override
    @Transactional
    public void deleteBoard(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Board with id " + id + " not found"));

        accessControlService.checkCanManageBoard(board);

        boardRepository.delete(board);
    }
}