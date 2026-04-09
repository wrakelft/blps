package ru.itmo.blps1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps1.entity.Pin;

import java.util.List;

public interface PinRepository extends JpaRepository<Pin, Long> {

    List<Pin> findAllByAuthorId(Long authorId);
}
