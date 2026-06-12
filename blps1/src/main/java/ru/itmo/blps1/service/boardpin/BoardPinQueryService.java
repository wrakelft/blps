package ru.itmo.blps1.service.boardpin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.blps1.entity.Board;
import ru.itmo.blps1.entity.BoardPin;
import ru.itmo.blps1.repository.BoardPinRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardPinQueryService implements BoardPinQueryServiceInt {

    private final BoardPinRepository boardPinRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Board> getBoardsContainingPin(Long pinId) {
        return boardPinRepository.findAllByPin_Id(pinId)
                .stream()
                .map(BoardPin::getBoard)
                .toList();
    }
}