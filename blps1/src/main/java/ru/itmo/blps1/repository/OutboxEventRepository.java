package ru.itmo.blps1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.blps1.entity.OutboxEvent;
import ru.itmo.blps1.entity.enums.OutboxEventStatus;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop200ByStatusInOrderByCreatedAtAsc(List<OutboxEventStatus> statuses);
}
