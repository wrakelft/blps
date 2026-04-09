package ru.itmo.blps1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.dto.board.BoardResponse;
import ru.itmo.blps1.dto.board.CreateBoardRequest;
import ru.itmo.blps1.entity.Board;
import ru.itmo.blps1.entity.User;
import ru.itmo.blps1.exception.ConflictException;
import ru.itmo.blps1.exception.NotFoundException;
import ru.itmo.blps1.mapper.BoardMapper;
import ru.itmo.blps1.repository.BoardRepository;
import ru.itmo.blps1.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final BoardMapper boardMapper;

    public BoardResponse createBoard(CreateBoardRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new NotFoundException("User with id " + request.getOwnerId() + " not found"));

        if (boardRepository.existsByOwnerIdAndName(request.getOwnerId(), request.getName())) {
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

    public List<BoardResponse> getAllBoards() {
        return boardRepository.findAll()
                .stream()
                .map(boardMapper::toResponse)
                .toList();
    }

    public BoardResponse getBoardById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Board with id " + id + " not found"));

        return boardMapper.toResponse(board);
    }

    public List<BoardResponse> getBoardsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        return boardRepository.findAllByOwnerId(userId)
                .stream()
                .map(boardMapper::toResponse)
                .toList();
    }
}