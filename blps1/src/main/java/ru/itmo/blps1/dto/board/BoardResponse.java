package ru.itmo.blps1.dto.board;

import lombok.Builder;
import lombok.Getter;
import ru.itmo.blps1.entity.enums.BoardModerationStatus;
import ru.itmo.blps1.entity.enums.BoardPrivacy;

import java.time.OffsetDateTime;

@Getter
@Builder
public class BoardResponse {

    private Long id;
    private String name;
    private String description;
    private BoardPrivacy privacy;
    private BoardModerationStatus moderationStatus;
    private OffsetDateTime createdAt;
    private Long ownerId;
}