package ru.itmo.blps1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.blps1.dto.boardpin.BoardPinResponse;
import ru.itmo.blps1.dto.pin.PinResponse;
import ru.itmo.blps1.entity.Board;
import ru.itmo.blps1.entity.BoardPin;
import ru.itmo.blps1.entity.Pin;
import ru.itmo.blps1.exception.ConflictException;
import ru.itmo.blps1.exception.NotFoundException;
import ru.itmo.blps1.mapper.BoardPinMapper;
import ru.itmo.blps1.mapper.PinMapper;
import ru.itmo.blps1.repository.BoardPinRepository;
import ru.itmo.blps1.repository.BoardRepository;
import ru.itmo.blps1.repository.PinRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardPinService {

    private final BoardRepository boardRepository;
    private final PinRepository pinRepository;
    private final BoardPinRepository boardPinRepository;
    private final BoardPinMapper boardPinMapper;
    private final PinMapper pinMapper;

    public BoardPinResponse saveExistingPinToBoard(Long boardId, Long pinId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("Board with id " + boardId + " not found"));

        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new NotFoundException("Pin with id " + pinId + " not found"));

        if (boardPinRepository.existsByBoardIdAndPinId(boardId, pinId)) {
            throw new ConflictException("Pin is already saved to this board");
        }

        BoardPin boardPin = BoardPin.builder()
                .board(board)
                .pin(pin)
                .build();

        BoardPin savedBoardPin = boardPinRepository.save(boardPin);
        return boardPinMapper.toResponse(savedBoardPin);
    }

    public List<PinResponse> getPinsByBoardId(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new NotFoundException("Board with id " + boardId + " not found");
        }

        return boardPinRepository.findAllByBoardId(boardId)
                .stream()
                .map(BoardPin::getPin)
                .map(pinMapper::toResponse)
                .toList();
    }
}