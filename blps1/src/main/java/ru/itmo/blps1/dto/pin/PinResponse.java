package ru.itmo.blps1.dto.pin;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class PinResponse {

    private Long id;
    private String title;
    private String description;
    private String contentUrl;
    private OffsetDateTime createdAt;
    private Long authorId;
}