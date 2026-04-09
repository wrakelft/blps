package ru.itmo.blps1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps1.entity.BoardPin;

import java.util.List;

public interface BoardPinRepository extends JpaRepository<BoardPin, Long> {

    boolean existsByBoardIdAndPinId(Long boardId, Long pinId);

    List<BoardPin> findAllByBoardId(Long boardId);

    List<BoardPin> findAllByPinId(Long pinId);
}
