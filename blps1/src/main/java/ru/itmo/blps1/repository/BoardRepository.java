package ru.itmo.blps1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps1.entity.Board;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findAllByOwnerId(Long ownerId);

    boolean existsByOwnerIdAndName(Long ownerId, String name);
}
