package ru.itmo.blps1.dto.boardpin;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class BoardPinResponse {

    private Long id;
    private Long boardId;
    private Long pinId;
    private OffsetDateTime savedAt;
}